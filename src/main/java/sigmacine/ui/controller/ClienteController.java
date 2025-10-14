package sigmacine.ui.controller;

import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.MenuButton;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import javafx.animation.FadeTransition;
import javafx.util.Duration;
import sigmacine.aplicacion.data.UsuarioDTO;
import sigmacine.aplicacion.service.VerHistorialService;
import sigmacine.aplicacion.session.Session;
import sigmacine.dominio.entity.Pelicula;
import sigmacine.infraestructura.configDataBase.DatabaseConfig;
import sigmacine.infraestructura.persistencia.jdbc.PeliculaRepositoryJdbc;
import sigmacine.infraestructura.persistencia.jdbc.UsuarioRepositoryJdbc;

import java.util.List;
import java.util.Set;
import java.util.Locale;

/**
 * Controlador principal del cliente (pagina_inicial.fxml).
 */
public class ClienteController {

    @FXML private Button btnPromoVerMas;
    @FXML private Button btnCartelera;
    @FXML private Button btnConfiteria;
    @FXML private Button btnSigmaCard;
    @FXML private Button btnCart;
    @FXML private MenuItem miCerrarSesion;
    @FXML private MenuItem miHistorial;
    @FXML private MenuButton menuPerfil;

    @FXML private javafx.scene.layout.StackPane promoPane;
    @FXML private ImageView imgPublicidad;
    @FXML private Label lblPromo;

    @FXML private TextField txtBuscar;
    @FXML private Button btnBuscar;

    @FXML private javafx.scene.layout.StackPane content;

    @FXML private ChoiceBox<String> cbCiudad;
    @FXML private Button btnSeleccionarCiudad;
    @FXML private Button btnIniciarSesion;
    @FXML private Label  lblUserName;
    @FXML private Button btnRegistrarse;

    @FXML private javafx.scene.layout.GridPane footerGrid;
    @FXML private ImageView imgCard1;
    @FXML private Label lblCard1;
    @FXML private ImageView imgCard2;
    @FXML private Label lblCard2;
    @FXML private ImageView imgCard3;
    @FXML private Label lblCard3;
    @FXML private ImageView imgCard4;
    @FXML private Label lblCard4;

    private UsuarioDTO usuario;
    private String ciudadSeleccionada;
    private ControladorControlador coordinador;
    // Evita disparar múltiples cargas de posters cuando la vista se crea desde distintos flujos
    private boolean postersRequested = false;

    // --- Carrito Overlay state ---
    private javafx.scene.Node carritoNode;           // contenido de verCarrito.fxml
    private javafx.scene.layout.Pane overlayCarrito; // capa con dimmer + carrito
    private javafx.scene.layout.StackPane carritoWrapper; // posicionable
    private boolean carritoVisible = false;
    private static final double CART_WIDTH = 600;
    private static final double CART_OFFSET_Y = 8;
    private static final double CART_MARGIN = 16;
    
    public void setCoordinador(ControladorControlador coordinador) {
        this.coordinador = coordinador;
    }


    public void init(UsuarioDTO usuario) { 
        this.usuario = usuario;
        esperarYCargarPeliculas();
        refreshSessionUI();
    }

    public void init(UsuarioDTO usuario, String ciudad) {
        this.usuario = usuario;
        this.ciudadSeleccionada = ciudad;
        esperarYCargarPeliculas();
    }
    
    private void esperarYCargarPeliculas() {
        if (postersRequested) {
            return;
        }
        postersRequested = true;
        // Intentar buscar un elemento que debería existir para verificar si la escena está lista
        if (btnCartelera != null && btnCartelera.getScene() != null) {
            Platform.runLater(() -> cargarPeliculasInicio());
        } else if (btnCartelera != null) {
            btnCartelera.sceneProperty().addListener((obs, oldScene, newScene) -> {
                if (newScene != null) {
                    Platform.runLater(() -> cargarPeliculasInicio());
                }
            });
        } else {
            new Thread(() -> {
                try {
                    Thread.sleep(1000); // Esperar 1 segundo
                    Platform.runLater(() -> cargarPeliculasInicio());
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }).start();
        }
    }
    public void initCiudad(UsuarioDTO usuario) {
        this.usuario = usuario;
        if (cbCiudad != null) {
            cbCiudad.getItems().setAll("Bogot\u00E1", "Medell\u00EDn", "Cali", "Barranquilla");
            cbCiudad.getSelectionModel().selectFirst();
        }
        refreshSessionUI();
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

    if (btnCartelera!= null) btnCartelera.setOnAction(e -> mostrarCartelera());
    if (btnConfiteria != null) btnConfiteria.setOnAction(e -> {});
    if (btnCart != null) btnCart.setOnAction(e -> toggleCarritoOverlay());
        if (miCerrarSesion != null) miCerrarSesion.setOnAction(e -> onLogout());
        
        if (miHistorial != null) miHistorial.setOnAction(e -> onVerHistorial()); // Llama al método corregido.

    // Disable profile menu when not logged in
    updateMenuPerfilState();

        if (btnIniciarSesion != null) btnIniciarSesion.setOnAction(e -> onIniciarSesion());
        if (btnRegistrarse  != null) {
            btnRegistrarse.setOnAction(e -> onRegistrarse());
            btnRegistrarse.setDisable(Session.isLoggedIn());
        }

        if (txtBuscar != null) {
            txtBuscar.setOnKeyPressed(ev -> { if (ev.getCode() == KeyCode.ENTER) doSearch(txtBuscar.getText()); });
            if (btnBuscar != null) btnBuscar.setOnAction(e -> doSearch(txtBuscar.getText()));
        }

        if (content != null) {
            content.getChildren().addListener((ListChangeListener<Node>) c -> updatePublicidadVisibility());
            updatePublicidadVisibility();
        }

        // Las películas se cargarán cuando se llame init() o init(usuario, ciudad)
        // Si esta vista se usa en `cliente_home.fxml` sin invocar init(), aseguramos la carga aquí.
        esperarYCargarPeliculas();
    }

    /**
     * Muestra la publicidad sólo cuando no hay sub-vistas cargadas en el StackPane `content`.
     * También marca los nodes como managed=false cuando están ocultos para que no ocupen espacio.
     */
    private void updatePublicidadVisibility() {
        try {
            boolean hasContent = content != null && !content.getChildren().isEmpty();
            boolean show = !hasContent;
            if (imgPublicidad != null) {
                imgPublicidad.setVisible(show);
                imgPublicidad.setManaged(show);
            }
            if (promoPane != null) {
                promoPane.setVisible(show);
                promoPane.setManaged(show);
            }
            // Also hide footer posters when another view is shown (e.g., historial)
            try {
                if (footerGrid == null) {
                    footerGrid = localizarFooterGridDesdeRoot();
                    // don't crash if still null
                }
                if (footerGrid != null) {
                    footerGrid.setVisible(show);
                    footerGrid.setManaged(show);
                    if (!show) {
                        // clear images so posters visually disappear and free resources
                        try {
                            for (javafx.scene.Node n : footerGrid.getChildren()) {
                                if (n instanceof ImageView) {
                                    ((ImageView) n).setImage(null);
                                }
                            }
                        } catch (Exception ignore) {}
                    } else {
                        // if footer becomes visible again, ensure posters are (re)loaded
                        Platform.runLater(() -> {
                            try { cargarPeliculasInicio(); } catch (Exception ignore) {}
                        });
                    }
                }
            } catch (Exception ignore) {}
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void ensureCarritoOverlay() {
        if (overlayCarrito != null) return;
        try {
            FXMLLoader fx = new FXMLLoader(getClass().getResource("/sigmacine/ui/views/verCarrito.fxml"));
            carritoNode = fx.load();

            carritoWrapper = new javafx.scene.layout.StackPane(carritoNode);
            carritoWrapper.setPrefWidth(CART_WIDTH);
            carritoWrapper.setPickOnBounds(true);

            javafx.scene.layout.Pane dimmer = new javafx.scene.layout.Pane();
            dimmer.setStyle("-fx-background-color: rgba(0,0,0,0.55);");
            dimmer.setPickOnBounds(true);
            dimmer.setOnMouseClicked(e -> hideCarritoOverlay());

            overlayCarrito = new javafx.scene.layout.Pane(dimmer, carritoWrapper);
            overlayCarrito.setVisible(false);
            overlayCarrito.setManaged(false);

            // content está dentro de un VBox dentro de BorderPane; su parent esperado es VBox
            // Para superponer, necesitamos un StackPane: content es StackPane, usamos su parent
            javafx.scene.layout.StackPane stackCentro = (javafx.scene.layout.StackPane) content.getParent();
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

    private void showCarritoOverlay() {
        ensureCarritoOverlay();
        javafx.scene.layout.StackPane stackCentro = (javafx.scene.layout.StackPane) content.getParent();

        // blur al fondo
        for (javafx.scene.Node n : stackCentro.getChildren()) {
            if (n != overlayCarrito) n.setEffect(new javafx.scene.effect.GaussianBlur(12));
        }

        // posicionar cerca del botón cart
        javafx.geometry.Bounds b = btnCart.localToScene(btnCart.getBoundsInLocal());
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

    private void hideCarritoOverlay() {
        if (overlayCarrito == null) return;
        javafx.scene.layout.StackPane stackCentro = (javafx.scene.layout.StackPane) content.getParent();
        overlayCarrito.setVisible(false);
        overlayCarrito.setManaged(false);
        for (javafx.scene.Node n : stackCentro.getChildren()) n.setEffect(null);
        carritoVisible = false;
    }

    private void toggleCarritoOverlay() {
        if (carritoVisible) hideCarritoOverlay();
        else showCarritoOverlay();
    }

    @FXML
    private void onBrandClick() {
        try {
            // If already on main, optionally no-op; else ensure we are on pagina_inicial
            Stage stage = null;
            try { stage = (Stage) (content != null ? content.getScene().getWindow() : btnCartelera.getScene().getWindow()); } catch (Exception ignore) {}
            if (stage == null) return;
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/sigmacine/ui/views/pagina_inicial.fxml"));
            javafx.scene.Parent root = loader.load();
            Object ctrl = loader.getController();
            if (ctrl instanceof ClienteController) {
                ClienteController c = (ClienteController) ctrl;
                c.setCoordinador(this.coordinador);
                c.init(this.usuario);
            }
            javafx.scene.Scene current = stage.getScene();
            double w = current != null ? current.getWidth() : 1000;
            double h = current != null ? current.getHeight() : 600;
            stage.setScene(new Scene(root, w, h));
            stage.setTitle("Sigma Cine");
            stage.setMaximized(true);
        } catch (Exception ex) { ex.printStackTrace(); }
    }
    
    private ImageView buscarImageView(String fxId) {
        try {
            if (btnCartelera != null && btnCartelera.getScene() != null) {
                return (ImageView) btnCartelera.getScene().lookup(fxId);
            }
            if (content != null && content.getScene() != null) {
                return (ImageView) content.getScene().lookup(fxId);
            }
        } catch (Exception ex) { ex.printStackTrace(); }
        return null;
    }
    private Label buscarLabel(String fxId) {
        try {
            if (btnCartelera != null && btnCartelera.getScene() != null) {
                return (Label) btnCartelera.getScene().lookup(fxId);
            }
            if (content != null && content.getScene() != null) {
                return (Label) content.getScene().lookup(fxId);
            }
        } catch (Exception ex) { ex.printStackTrace(); }
        return null;
    }

    private void cargarCard(ImageView img, Label lbl, Pelicula pelicula) {
        if (pelicula == null) return;
        if (lbl != null) lbl.setText(pelicula.getTitulo() != null ? pelicula.getTitulo() : "Sin t\u00EDtulo");
        if (img != null) {
            Image posterImage = null;
            if (pelicula.getPosterUrl() != null && !pelicula.getPosterUrl().isBlank()) {
                posterImage = resolveImage(pelicula.getPosterUrl());
            }
            if (posterImage == null) posterImage = resolveImage("placeholder.png");
            if (posterImage != null) {
                img.setImage(posterImage);
                img.setPreserveRatio(true);
                img.setFitWidth(220);
                img.setSmooth(true);
                img.setCache(true);
            }
        }
    }

    private Image resolveImage(String ref) {
        if (ref == null || ref.isBlank()) return null;
        try {
            String r = ref.trim();
            String lower = r.toLowerCase(Locale.ROOT);

            if (lower.startsWith("http://") || lower.startsWith("https://") || lower.startsWith("file:/")) {
                return new Image(r, true);
            }

            int slash = Math.max(r.lastIndexOf('/'), r.lastIndexOf('\\'));
            String fileName = (slash >= 0) ? r.substring(slash + 1) : r;

            java.net.URL res = getClass().getResource("/Images/" + fileName);
            if (res != null) return new Image(res.toExternalForm(), false);

            res = getClass().getResource(r.startsWith("/") ? r : ("/" + r));
            if (res != null) return new Image(res.toExternalForm(), false);

            java.io.File f = new java.io.File(r);
            if (f.exists()) return new Image(f.toURI().toString(), false);
        } catch (Exception ignored) {}
        return null;
    }

    private void onIniciarSesion() {
        
        // if already logged in, perform logout; otherwise show login
        if (Session.isLoggedIn()) {
            var a = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.CONFIRMATION);
            a.setTitle("Cerrar sesi\u00F3n");
            a.setHeaderText("\u00BFDesea cerrar sesi\u00F3n?");
            a.setContentText("Salir de la cuenta " + (Session.getCurrent() != null ? Session.getCurrent().getEmail() : ""));
            var opt = a.showAndWait();
            if (opt.isPresent() && opt.get() == javafx.scene.control.ButtonType.OK) onLogout();
            return;
        }
        if (coordinador != null) coordinador.mostrarLogin();
    }

    private void onRegistrarse() {
        
        if (Session.isLoggedIn()) {
            var a = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
            a.setTitle("Ya has iniciado sesi\u00F3n");
            a.setHeaderText(null);
            a.setContentText("Cierra sesi\u00F3n si deseas registrar una nueva cuenta.");
            a.showAndWait();
            return;
        }
        if (coordinador != null) coordinador.mostrarRegistro();
    }

    @SuppressWarnings("unused")
    private String safeCiudad() { return ciudadSeleccionada != null ? ciudadSeleccionada : "sin ciudad"; }

    private void onLogout() {
        
        // clear application session and update UI
        Session.clear();
        this.usuario = null;
        if (lblUserName != null) { lblUserName.setText(""); lblUserName.setVisible(false); }
        if (btnIniciarSesion != null) { btnIniciarSesion.setVisible(true); btnIniciarSesion.setText("Iniciar sesi\u00F3n"); }
        if (btnRegistrarse != null) btnRegistrarse.setDisable(false);
        updateMenuPerfilState();
        // Navigate back to main pagina_inicial.fxml so the app returns to a neutral state
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/sigmacine/ui/views/pagina_inicial.fxml"));
            Parent root = loader.load();
            ClienteController ctrl = loader.getController();
            // initialize without a user so the controller sets up default UI and posters
            ctrl.init(null);
            ctrl.setCoordinador(this.coordinador);

            Stage stage = (Stage) btnIniciarSesion.getScene().getWindow();
            javafx.scene.Scene current = stage.getScene();
            double w = current != null ? current.getWidth() : 900;
            double h = current != null ? current.getHeight() : 600;
            stage.setScene(new Scene(root, w > 0 ? w : 900, h > 0 ? h : 600));
            stage.setTitle("Sigma Cine - Cliente");
            stage.setMaximized(true);
        } catch (Exception ex) {
            // if navigation fails, ignore and keep current UI but logged out
            System.err.println("No se pudo navegar a pagina_inicial tras cerrar sesión: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void refreshSessionUI() {
        boolean logged = Session.isLoggedIn();
        if (logged) {
            var u = Session.getCurrent();
            String label = "";
            if (u != null) {
                if (u.getNombre() != null && !u.getNombre().isBlank()) label = u.getNombre();
                else if (u.getEmail() != null) {
                    String e = u.getEmail(); int at = e.indexOf('@'); label = at > 0 ? e.substring(0, at) : e;
                }
            }
            if (lblUserName != null) { lblUserName.setText(label); lblUserName.setVisible(true); }
            if (btnIniciarSesion != null) btnIniciarSesion.setVisible(false);
            if (btnRegistrarse  != null) btnRegistrarse.setDisable(true);
        } else {
            if (lblUserName != null) { lblUserName.setText(""); lblUserName.setVisible(false); }
            if (btnIniciarSesion != null) { btnIniciarSesion.setVisible(true); btnIniciarSesion.setText("Iniciar sesi\u00F3n"); }
            if (btnRegistrarse  != null) btnRegistrarse.setDisable(false);
        }
        updateMenuPerfilState();
    }

    private void updateMenuPerfilState() {
        try {
            boolean logged = Session.isLoggedIn();
            if (menuPerfil != null) {
                menuPerfil.setDisable(!logged);
                menuPerfil.setVisible(logged);
                menuPerfil.setManaged(logged);
            }
        } catch (Exception ex) { ex.printStackTrace(); }
    }
    
    @FXML private void onPromoVerMas() { }
    @FXML private void onCard1(){ }
    @FXML private void onCard2(){ }
    @FXML private void onCard3(){ }
    @FXML private void onCard4(){ }


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
            Scene current = stage.getScene();
            double w = current != null ? current.getWidth() : 900;
            double h = current != null ? current.getHeight() : 600;
            stage.setScene(new Scene(root, w > 0 ? w : 900, h > 0 ? h : 600));
            stage.show();
            stage.setMaximized(true);
        } catch (Exception ex) {
            throw new RuntimeException("Error cargando pagina_inicial.fxml", ex);
        }
    }

    @FXML
    private void onBuscarTop() { doSearch(txtBuscar != null ? txtBuscar.getText() : ""); }

    private void doSearch(String texto) {
        if (texto == null) texto = "";
        
        try {
            DatabaseConfig db = new DatabaseConfig();
            var repo = new PeliculaRepositoryJdbc(db);
            var resultados = repo.buscarPorTitulo(texto);

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/sigmacine/ui/views/resultados_busqueda.fxml"));
            Parent root = loader.load();
            ResultadosBusquedaController controller = loader.getController();
            controller.setCoordinador(this.coordinador);
            controller.setUsuario(this.usuario);
            controller.setResultados(resultados, texto);

            Stage stage = (Stage) content.getScene().getWindow();
            Scene current = stage.getScene();
            double w = current != null ? current.getWidth() : 900;
            double h = current != null ? current.getHeight() : 600;
            stage.setScene(new Scene(root, w > 0 ? w : 900, h > 0 ? h : 600));
            stage.setTitle("Resultados de b\u00FAsqueda");
            stage.setMaximized(true);
        } catch (Exception ex) {
            ex.printStackTrace();
            content.getChildren().setAll(new Label("Error cargando resultados: " + ex.getMessage()));
        }
    }

    @FXML
    private void onVerHistorial() {
        
        if (!Session.isLoggedIn()) {
            var a = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
            a.setTitle("Acceso denegado");
            a.setHeaderText(null);
            a.setContentText("Debes iniciar sesi\u00F3n para ver tu historial de compras.");
            a.showAndWait();
            return;
        }
        try {
            DatabaseConfig dbConfig = new DatabaseConfig();
            var usuarioRepo = new UsuarioRepositoryJdbc(dbConfig);
            var historialService = new VerHistorialService(usuarioRepo);
            
            // Carga el FXML (ruta verificada y correcta)
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/sigmacine/ui/views/verCompras.fxml"));
            VerHistorialController historialController = new VerHistorialController(historialService);
            historialController.setClienteController(this);

            if (this.usuario != null) historialController.setUsuarioEmail(this.usuario.getEmail());

            loader.setController(historialController);
            Parent historialView = loader.load();
            content.getChildren().setAll(historialView);
        } catch (Exception ex) {
            ex.printStackTrace();
            content.getChildren().setAll(new Label("Error cargando Historial: " + ex.getMessage()));
        }
    }

    public void mostrarCartelera() {
        try {
            java.net.URL url = getClass().getResource("/sigmacine/ui/views/cartelera.fxml");
            if (url == null) {
                content.getChildren().setAll(new Label("Error: No se encontró cartelera.fxml"));
                return;
            }
            FXMLLoader loader = new FXMLLoader(url);
            Parent carteleraView = loader.load();

            // Wire controller with session/coordinator if available
            try {
                Object ctrl = loader.getController();
                if (ctrl != null) {
                    try {
                        var m = ctrl.getClass().getMethod("setUsuario", sigmacine.aplicacion.data.UsuarioDTO.class);
                        if (m != null) m.invoke(ctrl, this.usuario);
                    } catch (NoSuchMethodException ignore) {}
                    try {
                        var m2 = ctrl.getClass().getMethod("setCoordinador", sigmacine.ui.controller.ControladorControlador.class);
                        if (m2 != null) m2.invoke(ctrl, this.coordinador);
                    } catch (NoSuchMethodException ignore) {}
                    try {
                        var rf = ctrl.getClass().getMethod("refreshSessionUI");
                        if (rf != null) rf.invoke(ctrl);
                    } catch (NoSuchMethodException ignore) {}
                }
            } catch (Exception ignore) {}

            Stage stage = (Stage) content.getScene().getWindow();
            Scene current = stage.getScene();
            double w = current != null ? current.getWidth() : 900;
            double h = current != null ? current.getHeight() : 600;
            stage.setScene(new Scene(carteleraView, w > 0 ? w : 900, h > 0 ? h : 600));
            stage.setTitle("Sigma Cine - Cartelera");
            stage.setMaximized(true);
        } catch (Exception e) {
            e.printStackTrace();
            content.getChildren().setAll(new Label("Error: No se pudo cargar la vista de cartelera."));
        }
    }

    /**
     * Carga las películas desde la BD y las muestra en las 4 cards del footer.
     * Llena los pósters y títulos de las películas.
     */
    private void cargarPeliculasInicio() {
        
        
        try {
            DatabaseConfig db = new DatabaseConfig();
            PeliculaRepositoryJdbc repo = new PeliculaRepositoryJdbc(db);
            
            // Buscar todas las películas (o filtrar por estado "En Cartelera")
            List<Pelicula> peliculas = repo.buscarPorTitulo(""); // buscar todas
            
            
            
            // Si no hay películas, salir
            if (peliculas == null || peliculas.isEmpty()) {
                
                return;
            }
            
            // Si los campos @FXML están null, intentar ubicar el GridPane desde la raíz de la escena
            if (footerGrid == null) {
                footerGrid = localizarFooterGridDesdeRoot();
                
            }
            // Si aún no lo tenemos, como fallback intentar lookup
            if (footerGrid == null) {
                
                buscarYCargarConLookup(peliculas);
                return;
            }
            
            // Independientemente de que existan placeholders en el FXML, renderizamos dinámicamente
            // para controlar tamaño y centrado con precisión.
            
            renderizarFooterDinamico(footerGrid, peliculas);
            return;
            
        } catch (Exception ex) {
            System.err.println("Error cargando películas para la página inicial: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
    
    /**
     * Localiza el GridPane del footer navegando la raíz de la escena en lugar de @FXML.
     */
    private javafx.scene.layout.GridPane localizarFooterGridDesdeRoot() {
        try {
            // Tomar cualquier nodo inyectado para llegar a la escena
            javafx.scene.Scene sc = null;
            if (promoPane != null) sc = promoPane.getScene();
            if (sc == null && content != null) sc = content.getScene();
            if (sc == null && imgPublicidad != null) sc = imgPublicidad.getScene();
            if (sc == null && btnBuscar != null) sc = btnBuscar.getScene();
            if (sc == null) return null;

            javafx.scene.Parent root = sc.getRoot();
            if (root instanceof javafx.scene.layout.BorderPane) {
                javafx.scene.layout.BorderPane bp = (javafx.scene.layout.BorderPane) root;
                javafx.scene.Node bottom = bp.getBottom();
                if (bottom instanceof javafx.scene.layout.HBox) {
                    for (javafx.scene.Node ch : ((javafx.scene.layout.HBox) bottom).getChildren()) {
                        if (ch instanceof javafx.scene.layout.GridPane) {
                            return (javafx.scene.layout.GridPane) ch;
                        }
                    }
                }
            }
        } catch (Exception ignore) {}
        return null;
    }

    /**
     * Construye tarjetas de películas en el footer al estilo de ResultadosBusqueda (programático).
     */
    private void renderizarFooterDinamico(javafx.scene.layout.GridPane grid, List<Pelicula> peliculas) {
        try {
            if (grid == null) return;
            grid.getChildren().clear();

            // Quitar restricciones de columnas/filas del FXML que estaban forzando tamaños pequeños
            if (grid.getColumnConstraints() != null) grid.getColumnConstraints().clear();
            if (grid.getRowConstraints() != null) grid.getRowConstraints().clear();

            // Mantener el GridPane al ancho de su contenido para centrar el bloque completo en el HBox
            grid.setMaxWidth(javafx.scene.layout.Region.USE_PREF_SIZE);
            grid.setPrefWidth(javafx.scene.layout.Region.USE_COMPUTED_SIZE);
            grid.setPadding(new javafx.geometry.Insets(12, 64, 12, 64)); // margen y ligero padding superior

            // Espaciados entre tarjetas
            grid.setHgap(64); // separación horizontal mayor
            grid.setVgap(16); // separación vertical mayor
            grid.setAlignment(javafx.geometry.Pos.CENTER);
            // Ligeramente elevado visualmente
            grid.setTranslateY(-30);

            // Mostrar solo 3 películas por defecto, centradas
            int max = Math.min(3, peliculas.size());
            // Con constraints limpiadas, usamos columnas 0..(max-1) y el GridPane centrará el bloque
            int startCol = 0;

            for (int i = 0; i < max; i++) {
                Pelicula p = peliculas.get(i);
                int col = startCol + i;

                // Poster más grande
                ImageView poster = new ImageView();
                poster.setPreserveRatio(true);
                poster.setFitWidth(220); // ancho mayor
                String posterRef = p.getPosterUrl();
                if (posterRef != null && !posterRef.isBlank()) {
                    Image img = resolveImage(posterRef);
                    if (img != null) poster.setImage(img);
                }
                javafx.scene.layout.GridPane.setColumnIndex(poster, col);
                javafx.scene.layout.GridPane.setRowIndex(poster, 0);
                javafx.scene.layout.GridPane.setMargin(poster, new javafx.geometry.Insets(0, 0, 8, 0));
                grid.getChildren().add(poster);

                // Título centrado y con wrap
                Label titulo = new Label(p.getTitulo() != null ? p.getTitulo() : "Sin título");
                titulo.setWrapText(true);
                titulo.setMaxWidth(220);
                titulo.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 16;");
                titulo.setAlignment(javafx.geometry.Pos.CENTER);
                titulo.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
                javafx.scene.layout.GridPane.setColumnIndex(titulo, col);
                javafx.scene.layout.GridPane.setRowIndex(titulo, 1);
                javafx.scene.layout.GridPane.setHalignment(titulo, javafx.geometry.HPos.CENTER);
                javafx.scene.layout.GridPane.setMargin(titulo, new javafx.geometry.Insets(0, 0, 8, 0));
                grid.getChildren().add(titulo);

                // Botón "Ver más" (opcional, sin acción por ahora)
                Button verMas = new Button("Ver más");
                verMas.setStyle("-fx-background-color: #993726; -fx-background-radius: 14; -fx-font-weight: bold; -fx-text-fill: #ffffff;");
                verMas.setPrefWidth(96);
                verMas.setPrefHeight(34);
                verMas.setOnAction(e -> abrirDetallePelicula(p));
                // When clicked, navigate to the detalle de película screen for this movie
                verMas.setOnAction(ev -> {
                    try {
                        // Prepare detail root
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/sigmacine/ui/views/verdetallepelicula.fxml"));
                        Parent detailRoot = loader.load();
                        Object ctrl = loader.getController();
                        if (ctrl instanceof sigmacine.ui.controller.VerDetallePeliculaController) {
                            sigmacine.ui.controller.VerDetallePeliculaController detalle = (sigmacine.ui.controller.VerDetallePeliculaController) ctrl;
                            detalle.setPelicula(p);
                            detalle.setUsuario(this.usuario);
                            detalle.setCoordinador(this.coordinador);
                        }

                        Stage stage = (Stage) content.getScene().getWindow();
                        javafx.scene.Scene current = stage.getScene();
                        double w = current != null ? current.getWidth() : 900;
                        double h = current != null ? current.getHeight() : 600;

                        // Fade out current root, then set new scene and fade in
                        Parent currentRoot = current != null ? current.getRoot() : null;
                        if (currentRoot != null) {
                            FadeTransition fadeOut = new FadeTransition(Duration.millis(220), currentRoot);
                            fadeOut.setFromValue(1.0);
                            fadeOut.setToValue(0.0);
                            fadeOut.setOnFinished(fe -> {
                                try {
                                    Scene newScene = new Scene(detailRoot, w > 0 ? w : 900, h > 0 ? h : 600);
                                    stage.setScene(newScene);
                                    stage.setTitle("Sigma Cine - Detalle película");
                                    stage.setMaximized(true);
                                    // start fade-in
                                    FadeTransition fadeIn = new FadeTransition(Duration.millis(220), newScene.getRoot());
                                    newScene.getRoot().setOpacity(0.0);
                                    fadeIn.setFromValue(0.0);
                                    fadeIn.setToValue(1.0);
                                    fadeIn.play();
                                } catch (Exception ex) { ex.printStackTrace(); }
                            });
                            fadeOut.play();
                        } else {
                            // fallback: just set scene without animation
                            stage.setScene(new Scene(detailRoot, w > 0 ? w : 900, h > 0 ? h : 600));
                            stage.setTitle("Sigma Cine - Detalle película");
                            stage.setMaximized(true);
                        }
                    } catch (Exception ex) {
                        System.err.println("Error abriendo detalle de película: " + ex.getMessage());
                        ex.printStackTrace();
                    }
                });
                javafx.scene.layout.GridPane.setColumnIndex(verMas, col);
                javafx.scene.layout.GridPane.setRowIndex(verMas, 2);
                javafx.scene.layout.GridPane.setHalignment(verMas, javafx.geometry.HPos.CENTER);
                grid.getChildren().add(verMas);
            }

            // Forzar layout una vez añadidos
            javafx.application.Platform.runLater(() -> {
                try { grid.applyCss(); grid.layout(); } catch (Exception ignore) {}
            });
        } catch (Exception ex) {
            System.err.println("Error renderizando footer dinámico: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
    
    private void buscarYCargarConLookup(List<Pelicula> peliculas) {
        
        try {
            // Intentar obtener los ImageView directamente por fx:id
            ImageView img1 = buscarImageView("#imgCard1");
            ImageView img2 = buscarImageView("#imgCard2");
            ImageView img3 = buscarImageView("#imgCard3");
            
            Label lbl1 = buscarLabel("#lblCard1");
            Label lbl2 = buscarLabel("#lblCard2");
            Label lbl3 = buscarLabel("#lblCard3");
            
            if (peliculas.size() > 0 && img1 != null) {
                cargarCard(img1, lbl1, peliculas.get(0));
            }
            if (peliculas.size() > 1 && img2 != null) {
                cargarCard(img2, lbl2, peliculas.get(1));
            }
            if (peliculas.size() > 2 && img3 != null) {
                cargarCard(img3, lbl3, peliculas.get(2));
            }
            
        } catch (Exception ex) {
            System.err.println("Error en buscarYCargarConLookup: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
    
    // buscarImageView and buscarLabel helpers are defined earlier in this class; duplicates removed.
    
    @SuppressWarnings("unused")
    private void buscarYCargarEnGrid(List<Pelicula> peliculas) {
        
        try {
            // Buscar todos los nodos del GridPane
            for (javafx.scene.Node node : footerGrid.getChildren()) {
                if (node instanceof ImageView) {
                    ImageView img = (ImageView) node;
                    Integer colIndex = javafx.scene.layout.GridPane.getColumnIndex(node);
                    Integer rowIndex = javafx.scene.layout.GridPane.getRowIndex(node);
                    
                    if (colIndex == null) colIndex = 0;
                    if (rowIndex == null) rowIndex = 0;
                    
                    // Solo procesar ImageViews en la fila 0 (pósters)
                    if (rowIndex == 0 && colIndex >= 0 && colIndex < peliculas.size()) {
                        Pelicula pelicula = peliculas.get(colIndex);
                        String posterUrl = pelicula.getPosterUrl();
                        
                        
                        if (posterUrl != null && !posterUrl.isBlank()) {
                            Image posterImage = resolveImage(posterUrl);
                            if (posterImage != null) {
                                img.setImage(posterImage);
                                
                            }
                        }
                    }
                } else if (node instanceof Label) {
                    Label lbl = (Label) node;
                    Integer colIndex = javafx.scene.layout.GridPane.getColumnIndex(node);
                    Integer rowIndex = javafx.scene.layout.GridPane.getRowIndex(node);
                    
                    if (colIndex == null) colIndex = 0;
                    if (rowIndex == null) rowIndex = 0;
                    
                    // Solo procesar Labels en la fila 1 (títulos)
                    if (rowIndex == 1 && colIndex >= 0 && colIndex < peliculas.size()) {
                        Pelicula pelicula = peliculas.get(colIndex);
                        lbl.setText(pelicula.getTitulo() != null ? pelicula.getTitulo() : "Sin título");
                        
                    }
                }
            }
        } catch (Exception ex) {
            System.err.println("Error buscando elementos en footerGrid: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
    
    // cargarCard and resolveImage helpers are defined earlier in this class; duplicates removed.

    /**
     * Abre la pantalla de detalle de película (contenidoCartelera.fxml) para la película indicada.
     * Mantiene la sesión de usuario y el coordinador para navegación.
     */
    @SuppressWarnings("unused")
    private void abrirDetallePelicula(Pelicula p) {
        if (p == null) return;
        try {
            var url = getClass().getResource("/sigmacine/ui/views/contenidoCartelera.fxml");
            if (url == null) throw new IllegalStateException("No se encontró contenidoCartelera.fxml");

            FXMLLoader loader = new FXMLLoader(url);
            Parent rootDetalle = loader.load();

            ContenidoCarteleraController ctrl = loader.getController();
            try { ctrl.setCoordinador(this.coordinador); } catch (Exception ignore) {}
            try { ctrl.setUsuario(this.usuario); } catch (Exception ignore) {}
            ctrl.setPelicula(p);

            Stage stage = null;
            if (content != null && content.getScene() != null) stage = (Stage) content.getScene().getWindow();
            else if (footerGrid != null && footerGrid.getScene() != null) stage = (Stage) footerGrid.getScene().getWindow();
            else if (btnCartelera != null && btnCartelera.getScene() != null) stage = (Stage) btnCartelera.getScene().getWindow();
            if (stage == null) return;

            Scene current = stage.getScene();
            double w = current != null ? current.getWidth() : 900;
            double h = current != null ? current.getHeight() : 600;
            stage.setScene(new Scene(rootDetalle, w > 0 ? w : 900, h > 0 ? h : 600));
            stage.setTitle(p.getTitulo() != null ? p.getTitulo() : "Detalle de Película");
            stage.setMaximized(true);
        } catch (Exception ex) {
            System.err.println("Error abriendo detalle: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    // Methods required by ContenidoCarteleraController embedding flow
    public boolean isSameScene(Button anyButton) {
        try {
            if (anyButton == null) return false;
            if (content != null && content.getScene() != null) {
                return anyButton.getScene() == content.getScene();
            }
            return false;
        } catch (Throwable t) {
            return false;
        }
    }

    public void mostrarAsientos(String titulo, String hora, Set<String> ocupados, Set<String> accesibles) {
        try {
            var url = getClass().getResource("/sigmacine/ui/views/asientos.fxml");
            if (url == null) return;

            FXMLLoader loader = new FXMLLoader(url);
            Parent root = loader.load();

            AsientosController ctrl = loader.getController();
            ctrl.setFuncion(titulo, hora, ocupados, accesibles);

            Stage stage = null;
            if (content != null && content.getScene() != null) stage = (Stage) content.getScene().getWindow();
            else if (btnCartelera != null && btnCartelera.getScene() != null) stage = (Stage) btnCartelera.getScene().getWindow();
            if (stage == null) return;

            Scene current = stage.getScene();
            double w = current != null ? current.getWidth() : 1100;
            double h = current != null ? current.getHeight() : 620;
            stage.setScene(new Scene(root, w, h));
            stage.setMaximized(true);
            stage.setTitle("Selecciona tus asientos");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}