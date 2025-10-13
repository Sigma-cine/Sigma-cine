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
    @FXML private VBox panelFunciones; // opcional si generas horarios por código
    @FXML private Button btnComprar;   // opcional
    @FXML private StackPane content;
    @FXML private Button btnCart;       // botón "Carrito" en la esquina superior derecha

    @FXML private Label lblGenero, lblClasificacion, lblDuracion, lblDirector, lblReparto;

    private Pane overlayCarrito;         
    private StackPane carritoWrapper;    
    private Parent carritoNode;           
    private boolean carritoVisible = false;

    private static final double CART_WIDTH  = 330;   
    private static final double CART_OFFSET_Y = 8;   
    private static final double CART_MARGIN   = 8;  

    private Pelicula pelicula;

    @FXML
    private void initialize() {
        if (btnComprar != null) {
            btnComprar.setOnAction(e -> {
                // TODO: navegación a flujo de compra con 'pelicula'
            });
        }
    }

    public void setPelicula(Pelicula p) {
        this.pelicula = p;

        // Poster
        String url = safe(p.getPosterUrl());
        if (!url.isEmpty()) {
            try { imgPoster.setImage(new Image(url, true)); } catch (Exception ignored) {}
        } else {
            imgPoster.setImage(null);
        }

        // Encabezado
        lblSinopsisTitulo.setText("SINOPSIS — " + safe(p.getTitulo(), "N/D"));

        // Ficha técnica (lo que mostraba el Alert)
        lblGenero.setText(safe(p.getGenero(), "N/D"));
        lblClasificacion.setText(safe(p.getClasificacion(), "N/D"));
        int dur = p.getDuracion();
        lblDuracion.setText(dur > 0 ? dur + " min" : "N/D");  // usa >0 si quieres mostrar N/D cuando venga 0

        lblDirector.setText(safe(p.getDirector(), "N/D"));
        lblReparto.setText(safe(join(p.getReparto()), "")); // puede ser largo

        // Sinopsis
        txtSinopsis.setText(safe(p.getSinopsis()));
    }

    /* ===== helpers ===== */

    private static String join(List<String> items) {
        return (items == null || items.isEmpty()) ? "" : String.join(", ", items);
    }

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
