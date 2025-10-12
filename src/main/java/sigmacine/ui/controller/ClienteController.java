package sigmacine.ui.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;

import javafx.geometry.Bounds;
import javafx.scene.Parent;
import javafx.scene.Scene;

import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;

import javafx.scene.effect.GaussianBlur;

import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;

import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;

import javafx.stage.Stage;
import javafx.stage.Window;

import sigmacine.aplicacion.data.UsuarioDTO;

// Dependencias de tu lógica
import sigmacine.aplicacion.service.VerHistorialService;
import sigmacine.infraestructura.configDataBase.DatabaseConfig;
import sigmacine.infraestructura.persistencia.jdbc.PeliculaRepositoryJdbc;
import sigmacine.infraestructura.persistencia.jdbc.UsuarioRepositoryJdbc;

public class ClienteController {

    // --- VISTA PRINCIPAL (pagina_inicial.fxml) ---
    @FXML private Button btnPromoVerMas;
    @FXML private Button btnCartelera;
    @FXML private Button btnConfiteria;
    @FXML private Button btnSigmaCard;
    @FXML private Button btnCart;
    @FXML private MenuItem miCerrarSesion;
    @FXML private MenuItem miHistorial;
    @FXML private StackPane promoPane;
    @FXML private ImageView imgPublicidad;
    @FXML private TextField txtBuscar;
    @FXML private Button btnBuscar;
    @FXML private StackPane content;

    @FXML private ChoiceBox<String> cbCiudad;
    @FXML private Button btnSeleccionarCiudad;

    private UsuarioDTO usuario;
    private String ciudadSeleccionada;

    // ============================
    // CARRITO: overlay incrustado
    // ============================
    private Pane overlayCarrito;          // dimmer + wrapper, ocupa todo el stack central
    private StackPane carritoWrapper;     // contenedor del panel del carrito (posicionado con layoutX/Y)
    private Parent carritoNode;           // raíz de verCarrito.fxml
    private boolean carritoVisible = false;

    // Dimensiones del panel (coincidir con tu verCarrito.fxml)
    private static final double CART_WIDTH  = 378;   // prefWidth del ScrollPane en tu FXML
    private static final double CART_OFFSET_Y = 8;   // separación vertical bajo el botón Cart
    private static final double CART_MARGIN   = 8;   // margen de seguridad a bordes del stack

    // ============================
    // Inicialización y lógica base
    // ============================

    public void init(UsuarioDTO usuario) { this.usuario = usuario; }
    public void init(UsuarioDTO usuario, String ciudad) {
        this.usuario = usuario;
        this.ciudadSeleccionada = ciudad;
    }
    public void initCiudad(UsuarioDTO usuario) {
        this.usuario = usuario;
        if (cbCiudad != null) {
            cbCiudad.getItems().setAll("Bogotá", "Medellín", "Cali", "Barranquilla");
            cbCiudad.getSelectionModel().selectFirst();
        }
    }

    @FXML
    private void initialize() {
        if (promoPane != null && imgPublicidad != null) {
            imgPublicidad.fitWidthProperty().bind(promoPane.widthProperty());
            imgPublicidad.setFitHeight(110);
            imgPublicidad.setPreserveRatio(true);
        }

        if (btnSeleccionarCiudad != null) {
            btnSeleccionarCiudad.setOnAction(e -> onSeleccionarCiudad());
        }

        if (btnCartelera != null) btnCartelera.setOnAction(e -> System.out.println("Ir a Cartelera (" + safeCiudad() + ")"));
        if (btnConfiteria != null) btnConfiteria.setOnAction(e -> System.out.println("Ir a Confitería (" + safeCiudad() + ")"));
        if (miCerrarSesion != null) miCerrarSesion.setOnAction(e -> onLogout());
        if (miHistorial != null)     miHistorial.setOnAction(e -> onVerHistorial());

        // Abrir/cerrar el carrito (overlay angosto pegado al botón Cart)
        if (btnCart != null) btnCart.setOnAction(e -> toggleCarritoOverlay());

        if (txtBuscar != null) {
            txtBuscar.setOnKeyPressed(ev -> {
                if (ev.getCode() == KeyCode.ENTER) {
                    doSearch(txtBuscar.getText());
                }
            });
            if (btnBuscar != null) btnBuscar.setOnAction(e -> doSearch(txtBuscar.getText()));
        }
    }

    private String safeCiudad() {
        return ciudadSeleccionada != null ? ciudadSeleccionada : "sin ciudad";
    }

    private void onLogout() {
        System.out.println("Cerrar sesión de: " + (usuario != null ? usuario.getEmail() : "desconocido"));
    }

    @FXML private void onPromoVerMas() { System.out.println("Promoción → Ver más (" + safeCiudad() + ")"); }
    @FXML private void onCard1(){ System.out.println("Card 1 → Ver más (" + safeCiudad() + ")"); }
    @FXML private void onCard2(){ System.out.println("Card 2 → Ver más (" + safeCiudad() + ")"); }
    @FXML private void onCard3(){ System.out.println("Card 3 → Ver más (" + safeCiudad() + ")"); }
    @FXML private void onCard4(){ System.out.println("Card 4 → Ver más (" + safeCiudad() + ")"); }

    private void onSeleccionarCiudad() {
        String ciudad = (cbCiudad != null) ? cbCiudad.getValue() : null;
        if (ciudad == null || ciudad.isBlank()) return;

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/sigmacine/ui/views/pagina_inicial.fxml"));
            Parent root = loader.load();
            ClienteController controller = loader.getController();
            controller.init(this.usuario, ciudad);

            Stage stage = (Stage) btnSeleccionarCiudad.getScene().getWindow();
            stage.setTitle("Sigma Cine - Cliente (" + ciudad + ")");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception ex) {
            throw new RuntimeException("Error cargando pagina_inicial.fxml", ex);
        }
    }

    @FXML
    private void onBuscarTop() {
        doSearch(txtBuscar != null ? txtBuscar.getText() : "");
    }

    private void doSearch(String texto) {
        if (texto == null) texto = "";
        System.out.println("doSearch invoked with: '" + texto + "'");
        try {
            DatabaseConfig db = new DatabaseConfig();
            var repo = new PeliculaRepositoryJdbc(db);
            var resultados = repo.buscarPorTitulo(texto);

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/sigmacine/ui/views/resultados_busqueda.fxml"));
            Parent root = loader.load();
            ResultadosBusquedaController controller = loader.getController();
            controller.setResultados(resultados, texto);

            Stage stage = (Stage) content.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Resultados de búsqueda");

        } catch (Exception ex) {
            System.err.println("Error cargando resultados_busqueda.fxml: " + ex.getMessage());
            ex.printStackTrace();
            throw new RuntimeException("Error cargando resultados_busqueda.fxml", ex);
        }
    }

    @FXML
    private void onVerHistorial() {
        System.out.println("Navegando a Historial de Compras.");
        try {
            DatabaseConfig dbConfig = new DatabaseConfig();
            var usuarioRepo = new UsuarioRepositoryJdbc(dbConfig);
            var historialService = new VerHistorialService(usuarioRepo);

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/sigmacine/ui/views/VerCompras.fxml"));

            VerHistorialController historialController = new VerHistorialController(historialService);
            historialController.setClienteController(this);

            if (this.usuario != null) {
                historialController.setUsuarioEmail(this.usuario.getEmail());
            }

            loader.setController(historialController);

            Parent historialView = loader.load();

            content.getChildren().setAll(historialView);

        } catch (Exception ex) {
            System.err.println("Error cargando HistorialDeCompras.fxml: " + ex.getMessage());
            ex.printStackTrace();
            content.getChildren().setAll(new Label("Error cargando Historial: " + ex.getMessage()));
        }
    }

    public void mostrarCartelera() {
        System.out.println("Volviendo a Cartelera (Inicio).");
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/sigmacine/ui/views/pagina_inicial.fxml"));
            Parent carteleraView = loader.load();

            ClienteController controller = loader.getController();
            controller.init(this.usuario, this.ciudadSeleccionada);
            content.getChildren().setAll(carteleraView);
        } catch (Exception e) {
            content.getChildren().setAll(new Label("Error: No se pudo cargar la vista de inicio."));
            e.printStackTrace();
        }
    }

    // =========================================================
    // CARRITO: overlay angosto, pegado al botón "Cart"
    // =========================================================

    // Crea el overlay sobre el StackPane central (hermano de promoPane)
    private void ensureCarritoOverlay() {
        if (overlayCarrito != null) return;

        try {
            // 1) Cargar verCarrito.fxml (tu panel angosto con fondo negro y scroll)
            FXMLLoader fx = new FXMLLoader(getClass().getResource("/sigmacine/ui/views/verCarrito.fxml"));
            carritoNode = fx.load();

            // 2) Wrapper para posicionar el panel por coordenadas (layoutX/Y)
            carritoWrapper = new StackPane(carritoNode);
            carritoWrapper.setPrefWidth(CART_WIDTH);
            carritoWrapper.setPickOnBounds(true);

            // 3) Dimmer que cubre todo el stack central
            Pane dimmer = new Pane();
            dimmer.setStyle("-fx-background-color: rgba(0,0,0,0.55);");
            dimmer.setPickOnBounds(true);
            dimmer.setOnMouseClicked(e -> hideCarritoOverlay()); // cerrar al clicar fuera

            // 4) Overlay absoluto (dimmer + wrapper)
            overlayCarrito = new Pane(dimmer, carritoWrapper);
            overlayCarrito.setVisible(false);
            overlayCarrito.setManaged(false);

            // 5) Insertar overlay sobre el StackPane PADRE del centro
            StackPane stackCentro = (StackPane) content.getParent();
            dimmer.prefWidthProperty().bind(stackCentro.widthProperty());
            dimmer.prefHeightProperty().bind(stackCentro.heightProperty());
            overlayCarrito.prefWidthProperty().bind(stackCentro.widthProperty());
            overlayCarrito.prefHeightProperty().bind(stackCentro.heightProperty());
            stackCentro.getChildren().add(overlayCarrito); // al final → por encima

            // 6) Cerrar con ESC
            overlayCarrito.addEventFilter(javafx.scene.input.KeyEvent.KEY_PRESSED, ev -> {
                if (ev.getCode() == KeyCode.ESCAPE) hideCarritoOverlay();
            });

        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException("No se pudo crear el overlay del carrito (verCarrito.fxml)", ex);
        }
    }

    // Mostrar overlay: posiciona debajo del botón "Cart", alineado a la derecha del botón
    public void showCarritoOverlay() {
        ensureCarritoOverlay();
        StackPane stackCentro = (StackPane) content.getParent();

        // Blur a todo lo de atrás
        for (javafx.scene.Node n : stackCentro.getChildren()) {
            if (n != overlayCarrito) n.setEffect(new GaussianBlur(12));
        }

        // Coordenadas del botón Cart (en escena) → al sistema de stackCentro
        Bounds b = btnCart.localToScene(btnCart.getBoundsInLocal());
        javafx.geometry.Point2D p = stackCentro.sceneToLocal(b.getMaxX(), b.getMaxY());

        // Calcular posición: borde derecho del carrito alineado con borde derecho del botón
        double x = p.getX() - CART_WIDTH;
        double y = p.getY() + CART_OFFSET_Y;

        // Limitar a márgenes seguros del stack
        x = Math.max(CART_MARGIN, Math.min(x, stackCentro.getWidth() - CART_WIDTH - CART_MARGIN));
        y = Math.max(CART_MARGIN, Math.min(y, stackCentro.getHeight() - carritoWrapper.prefHeight(-1) - CART_MARGIN));

        carritoWrapper.setLayoutX(x);
        carritoWrapper.setLayoutY(y);

        overlayCarrito.setVisible(true);
        overlayCarrito.setManaged(true);
        overlayCarrito.requestFocus();
        carritoVisible = true;
    }

    // Ocultar overlay y quitar blur
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

    // Toggle desde el botón "Cart"
    public void toggleCarritoOverlay() {
        if (carritoVisible) hideCarritoOverlay();
        else showCarritoOverlay();
    }

    // (Utilitario por si lo necesitas más adelante)
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
