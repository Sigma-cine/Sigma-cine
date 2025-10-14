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


public class DetallePeliculaController {

    @FXML private ImageView imgPoster;
    @FXML private TextArea txtSinopsis;
    @FXML private Label lblSinopsisTitulo;
    @FXML private javafx.scene.layout.StackPane trailerContainer;
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
                // TODO: navegación a flujo de compra con 'pelicula'
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

        // Trailer: attempt to embed a WebView via reflection if javafx.web is available;
        // otherwise provide a fallback button that opens the trailer in the system browser.
        try {
            trailerContainer.getChildren().clear();
            String trailer = safe(p.getTrailer());
            if (trailer.isEmpty()) {
                javafx.scene.control.Label none = new javafx.scene.control.Label("No hay trailer disponible");
                none.setStyle("-fx-text-fill: #cbd5e1;");
                trailerContainer.getChildren().add(none);
            } else {
                try {
                    // try to load WebView class via reflection to avoid compile dependency
                    Class<?> webViewClass = Class.forName("javafx.scene.web.WebView");
                    Object webView = webViewClass.getDeclaredConstructor().newInstance();
                    // get engine and load content
                    java.lang.reflect.Method getEngine = webViewClass.getMethod("getEngine");
                    Object engine = getEngine.invoke(webView);
                    String html = null;
                    if (trailer.contains("youtube.com") || trailer.contains("youtu.be")) {
                        String id = null;
                        if (trailer.contains("v=")) {
                            int i = trailer.indexOf("v=") + 2;
                            int amp = trailer.indexOf('&', i);
                            id = amp > 0 ? trailer.substring(i, amp) : trailer.substring(i);
                        } else if (trailer.contains("youtu.be/")) {
                            int i = trailer.indexOf("youtu.be/") + 9;
                            int q = trailer.indexOf('?', i);
                            id = q > 0 ? trailer.substring(i, q) : trailer.substring(i);
                        }
                        if (id != null && !id.isEmpty()) {
                            html = "<html><body style='margin:0;background:#000;'><iframe width='100%' height='100%' src='https://www.youtube.com/embed/" + id + "' frameborder='0' allow='accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture' allowfullscreen></iframe></body></html>";
                        }
                    }
                    if (html != null) {
                        java.lang.reflect.Method loadContent = engine.getClass().getMethod("loadContent", String.class);
                        loadContent.invoke(engine, html);
                    } else {
                        java.lang.reflect.Method load = engine.getClass().getMethod("load", String.class);
                        load.invoke(engine, trailer.startsWith("http") ? trailer : "data:text/html," + trailer);
                    }
                    trailerContainer.getChildren().add((javafx.scene.Node) webView);
                } catch (ClassNotFoundException cnf) {
                    // javafx.web not present — fallback to button
                    javafx.scene.control.Button open = new javafx.scene.control.Button("Ver trailer");
                    open.setOnAction(ev -> {
                        try { java.awt.Desktop.getDesktop().browse(new java.net.URI(trailer)); } catch (Exception e) { e.printStackTrace(); }
                    });
                    trailerContainer.getChildren().add(open);
                }
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
