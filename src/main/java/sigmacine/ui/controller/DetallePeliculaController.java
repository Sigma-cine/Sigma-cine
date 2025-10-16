package sigmacine.ui.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import sigmacine.dominio.entity.Pelicula;
import java.util.List;
import java.util.ArrayList;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import sigmacine.infraestructura.configDataBase.DatabaseConfig;


public class DetallePeliculaController {

    @FXML private ImageView imgPoster;
    @FXML private TextArea txtSinopsis;
    @FXML private Label lblSinopsisTitulo;
    @FXML private javafx.scene.layout.StackPane trailerContainer;
    @FXML private javafx.scene.layout.HBox trailerButtons;
    @FXML private Label lblTituloPelicula;
    @FXML private VBox panelFunciones; // opcional si generas horarios por código
    @FXML private Button btnComprar;   // opcional
    @FXML private Button btnRegresarBusqueda;
    @FXML private Button btnRegresarHome;

    @FXML private Label lblGenero, lblClasificacion, lblDuracion, lblDirector, lblReparto;

    private Pelicula pelicula;
    // data to return to results screen
    private List<Pelicula> backResults;
    private String backTexto;

    public void setBackResults(List<Pelicula> results, String texto) {
        this.backResults = results;
        this.backTexto = texto;
    }

    @FXML
    private void initialize() {
        if (btnComprar != null) {
            btnComprar.setOnAction(e -> {
                // Añadir un boleto de ejemplo al carrito (id=null, precio por defecto 1000 => $10.00)
                try {
                    sigmacine.aplicacion.service.CarritoService cs = sigmacine.aplicacion.service.CarritoService.getInstance();
                    sigmacine.dominio.entity.Boleto b = new sigmacine.dominio.entity.Boleto();
                    b.setPelicula(safe(pelicula != null ? pelicula.getTitulo() : "Película"));
                    b.setSala("General");
                    b.setHorario("Por definir");
                    b.setAsiento("N/A");
                    b.setPrecio(1000); // 1000 centavos => $10.00
                    cs.addBoleto(b);
                    // Si el overlay del carrito está presente, intentar refrescar su controlador
                    try {
                        javafx.scene.Node parent = btnComprar.getScene().lookup("#carritoRoot");
                        if (parent != null) {
                            Object ctrl = parent.getProperties().get("fx:controller");
                            if (ctrl instanceof sigmacine.ui.controller.VerCarritoController) {
                                ((sigmacine.ui.controller.VerCarritoController) ctrl).refresh();
                            }
                        }
                    } catch (Exception ex) { /* ignore */ }
                } catch (Exception ex) { ex.printStackTrace(); }
            });
        }
        if (btnRegresarBusqueda != null) {
            btnRegresarBusqueda.setOnAction(e -> {
                try {
                    var loader = new javafx.fxml.FXMLLoader(getClass().getResource("/sigmacine/ui/views/resultados_busqueda.fxml"));
                    javafx.scene.Parent root = loader.load();
                    ResultadosBusquedaController ctrl = loader.getController();
                    // if we have back data, restore it
                    if (this.backResults != null) {
                        ctrl.setResultados(this.backResults, this.backTexto != null ? this.backTexto : "");
                    }
                    javafx.stage.Stage stage = (javafx.stage.Stage) btnRegresarBusqueda.getScene().getWindow();
                    // preserve window size
                    javafx.scene.Scene current = stage.getScene();
                    double w = current != null ? current.getWidth() : 900;
                    double h = current != null ? current.getHeight() : 600;
                    stage.setScene(new javafx.scene.Scene(root, w > 0 ? w : 900, h > 0 ? h : 600));
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });
        }
        if (btnRegresarHome != null) {
            btnRegresarHome.setOnAction(e -> {
                try {
                    var loader = new javafx.fxml.FXMLLoader(getClass().getResource("/sigmacine/ui/views/pagina_inicial.fxml"));
                    javafx.scene.Parent root = loader.load();
                    javafx.stage.Stage stage = (javafx.stage.Stage) btnRegresarHome.getScene().getWindow();
                    javafx.scene.Scene current = stage.getScene();
                    double w = current != null ? current.getWidth() : 900;
                    double h = current != null ? current.getHeight() : 600;
                    stage.setScene(new javafx.scene.Scene(root, w > 0 ? w : 900, h > 0 ? h : 600));
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });
        }
    }

    public void setPelicula(Pelicula p) {
        this.pelicula = p;

        // Poster
        String url = safe(p.getPosterUrl());
        if (!url.isEmpty()) {
            // try classpath resource first
            try {
                var res = getClass().getResourceAsStream("/Images/" + url);
                if (res != null) {
                    imgPoster.setImage(new Image(res));
                } else {
                    // try URL (remote)
                    imgPoster.setImage(new Image(url, true));
                }
            } catch (Exception ex) {
                // fallback: null image
                imgPoster.setImage(null);
            }
        } else {
            imgPoster.setImage(null);
        }

        // Encabezado
        lblSinopsisTitulo.setText("SINOPSIS — " + safe(p.getTitulo(), "N/D"));
        lblTituloPelicula.setText(safe(p.getTitulo(), "N/D"));

        // Ficha técnica (lo que mostraba el Alert)
        lblGenero.setText(safe(p.getGenero(), "N/D"));
        lblClasificacion.setText(safe(p.getClasificacion(), "N/D"));
        int dur = p.getDuracion();
        lblDuracion.setText(dur > 0 ? dur + " min" : "N/D");  // usa >0 si quieres mostrar N/D cuando venga 0

        lblDirector.setText(safe(p.getDirector(), "N/D"));
    lblReparto.setText(safe(p.getReparto())); // puede ser largo

        // Sinopsis
        txtSinopsis.setText(safe(p.getSinopsis()));

        // Trailer: load trailers from PELICULA_TRAILER table (if present) and show selector buttons.
        try {
            trailerContainer.getChildren().clear();
            trailerButtons.getChildren().clear();
            List<String> trailers = new ArrayList<>();
            // primary trailer field (legacy)
            if (p.getTrailer() != null && !p.getTrailer().isBlank()) trailers.add(p.getTrailer());
            // load additional trailers from DB
            try (Connection cn = new DatabaseConfig().getConnection();
                 PreparedStatement ps = cn.prepareStatement("SELECT URL FROM PELICULA_TRAILER WHERE PELICULA_ID = ?")) {
                ps.setInt(1, p.getId());
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        String u = rs.getString("URL");
                        if (u != null && !u.isBlank() && !trailers.contains(u)) trailers.add(u);
                    }
                }
            } catch (Exception dbex) { /* ignore DB trailer errors */ }

            if (trailers.isEmpty()) {
                javafx.scene.control.Label none = new javafx.scene.control.Label("No hay trailer disponible");
                none.setStyle("-fx-text-fill: #cbd5e1;");
                trailerContainer.getChildren().add(none);
                return;
            }

            // create a WebView (via reflection to be safe) and a loader function
            Object webView = null;
            Object engine = null;
            final boolean[] webAvailable = new boolean[] { true };
            try {
                Class<?> webViewClass = Class.forName("javafx.scene.web.WebView");
                webView = webViewClass.getDeclaredConstructor().newInstance();
                java.lang.reflect.Method getEngine = webViewClass.getMethod("getEngine");
                engine = getEngine.invoke(webView);
            } catch (ClassNotFoundException cnf) {
                webAvailable[0] = false;
            }

            final Object finalWebView = webView;
            final Object finalEngine = engine;

            for (int i = 0; i < trailers.size(); i++) {
                final String trailerUrl = trailers.get(i);
                javafx.scene.control.Button b = new javafx.scene.control.Button("Trailer " + (i + 1));
                b.setOnAction(ev -> {
                    try {
                        trailerContainer.getChildren().clear();
                        if (webAvailable[0] && finalWebView != null && finalEngine != null) {
                            // generate embed HTML for youtube
                            String html = null;
                            if (trailerUrl.contains("youtube.com") || trailerUrl.contains("youtu.be")) {
                                String id = null;
                                if (trailerUrl.contains("v=")) {
                                    int ii = trailerUrl.indexOf("v=") + 2;
                                    int amp = trailerUrl.indexOf('&', ii);
                                    id = amp > 0 ? trailerUrl.substring(ii, amp) : trailerUrl.substring(ii);
                                } else if (trailerUrl.contains("youtu.be/")) {
                                    int ii = trailerUrl.indexOf("youtu.be/") + 9;
                                    int q = trailerUrl.indexOf('?', ii);
                                    id = q > 0 ? trailerUrl.substring(ii, q) : trailerUrl.substring(ii);
                                }
                                if (id != null && !id.isEmpty()) {
                                    html = "<html><body style='margin:0;background:#000;'><iframe width='100%' height='100%' src='https://www.youtube.com/embed/" + id + "' frameborder='0' allow='accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture' allowfullscreen></iframe></body></html>";
                                }
                            }
                            if (html != null) {
                                java.lang.reflect.Method loadContent = finalEngine.getClass().getMethod("loadContent", String.class);
                                loadContent.invoke(finalEngine, html);
                            } else {
                                java.lang.reflect.Method load = finalEngine.getClass().getMethod("load", String.class);
                                load.invoke(finalEngine, trailerUrl.startsWith("http") ? trailerUrl : "data:text/html," + trailerUrl);
                            }
                            trailerContainer.getChildren().add((javafx.scene.Node) finalWebView);
                        } else {
                            javafx.scene.control.Button open = new javafx.scene.control.Button("Abrir trailer en navegador");
                            open.setOnAction(ae -> { try { java.awt.Desktop.getDesktop().browse(new java.net.URI(trailerUrl)); } catch (Exception e) { e.printStackTrace(); } });
                            trailerContainer.getChildren().add(open);
                        }
                    } catch (Exception ex) { ex.printStackTrace(); }
                });
                trailerButtons.getChildren().add(b);
                // auto-click first
                if (i == 0) b.fire();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /* ===== helpers ===== */

    private static String safe(String s) {
        return (s == null || s.trim().equalsIgnoreCase("null")) ? "" : s.trim();
    }

    private static String safe(String s, String alt) {
        String t = safe(s);
        return t.isEmpty() ? alt : t;
    }
}
