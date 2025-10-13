package sigmacine.ui.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Bounds;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.Window;
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
    @FXML private StackPane content;
    @FXML private Button btnCart;       // botón "Carrito" en la esquina superior derecha
    @FXML private Button btnRegresarBusqueda;
    @FXML private Button btnRegresarHome;

    @FXML private Label lblGenero, lblClasificacion, lblDuracion, lblDirector, lblReparto;

    private Pane overlayCarrito;         
    private StackPane carritoWrapper;    
    private Parent carritoNode;           
    private boolean carritoVisible = false;

    private static final double CART_WIDTH  = 330;   
    private static final double CART_OFFSET_Y = 8;   
    private static final double CART_MARGIN   = 8;  

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

        private void ensureCarritoOverlay() {
        if (overlayCarrito != null) return;

        try {
            FXMLLoader fx = new FXMLLoader(getClass().getResource("/sigmacine/ui/views/verCarrito.fxml"));
            carritoNode = fx.load();

            carritoWrapper = new StackPane(carritoNode);
            carritoWrapper.setPrefWidth(CART_WIDTH);
            carritoWrapper.setPickOnBounds(true);

            Pane dimmer = new Pane();
            dimmer.setStyle("-fx-background-color: rgba(0,0,0,0.55);");
            dimmer.setPickOnBounds(true);
            dimmer.setOnMouseClicked(e -> hideCarritoOverlay());

            overlayCarrito = new Pane(dimmer, carritoWrapper);
            overlayCarrito.setVisible(false);
            overlayCarrito.setManaged(false);

            StackPane stackCentro = (StackPane) content.getParent();
            dimmer.prefWidthProperty().bind(stackCentro.widthProperty());
            dimmer.prefHeightProperty().bind(stackCentro.heightProperty());
            overlayCarrito.prefWidthProperty().bind(stackCentro.widthProperty());
            overlayCarrito.prefHeightProperty().bind(stackCentro.heightProperty());
            stackCentro.getChildren().add(overlayCarrito);

            overlayCarrito.addEventFilter(javafx.scene.input.KeyEvent.KEY_PRESSED, ev -> {
                if (ev.getCode() == KeyCode.ESCAPE) hideCarritoOverlay();
            });

        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException("No se pudo crear el overlay del carrito (verCarrito.fxml)", ex);
        }
    }

    public void showCarritoOverlay() {
        ensureCarritoOverlay();
        StackPane stackCentro = (StackPane) content.getParent();

        for (javafx.scene.Node n : stackCentro.getChildren()) {
            if (n != overlayCarrito) n.setEffect(new GaussianBlur(12));
        }

        Bounds b = btnCart.localToScene(btnCart.getBoundsInLocal());
        javafx.geometry.Point2D p = stackCentro.sceneToLocal(b.getMaxX(), b.getMaxY());

        double x = p.getX() - CART_WIDTH;
        double y = p.getY() + CART_OFFSET_Y;

        x = Math.max(CART_MARGIN, Math.min(x, stackCentro.getWidth() - CART_WIDTH - CART_MARGIN));
        y = Math.max(CART_MARGIN, Math.min(y, stackCentro.getHeight() - carritoWrapper.prefHeight(-1) - CART_MARGIN));

        carritoWrapper.setLayoutX(x);
        carritoWrapper.setLayoutY(y);

        overlayCarrito.setVisible(true);
        overlayCarrito.setManaged(true);
        overlayCarrito.requestFocus();
        carritoVisible = true;
    }

    public void hideCarritoOverlay() {
        if (overlayCarrito == null) return;
        StackPane stackCentro = (StackPane) content.getParent();

        overlayCarrito.setVisible(false);
        overlayCarrito.setManaged(false);

        for (javafx.scene.Node n : stackCentro.getChildren()) {
            n.setEffect(null);
        }
        carritoVisible = false;
    }

    public void toggleCarritoOverlay() {
        if (carritoVisible) hideCarritoOverlay();
        else showCarritoOverlay();
    }

    @SuppressWarnings("unused")
    private void positionNearButton(Button button, Stage popup) {
        Bounds b = button.localToScreen(button.getBoundsInLocal());
        if (b != null) {
            double prefW = 600;
            double x = b.getMaxX() - prefW;
            double y = b.getMaxY() + 10;
            popup.setX(x);
            popup.setY(y);
        } else {
            Window w = button.getScene().getWindow();
            popup.setX(w.getX() + (w.getWidth() - 600) / 2);
            popup.setY(w.getY() + (w.getHeight() - 400) / 2);
        }
    }
}
