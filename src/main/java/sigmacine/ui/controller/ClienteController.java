package sigmacine.ui.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.MenuButton;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import sigmacine.aplicacion.data.UsuarioDTO;
import javafx.scene.image.ImageView;
import javafx.collections.ListChangeListener;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.application.Platform;
import javafx.animation.FadeTransition;
import javafx.util.Duration;
// Dependencias necesarias para instanciación manual
import sigmacine.aplicacion.service.VerHistorialService;
import sigmacine.infraestructura.configDataBase.DatabaseConfig;
import sigmacine.infraestructura.persistencia.jdbc.PeliculaRepositoryJdbc;
import sigmacine.infraestructura.persistencia.jdbc.UsuarioRepositoryJdbc; 
import sigmacine.ui.controller.ControladorControlador;
import sigmacine.aplicacion.session.Session;
import sigmacine.dominio.entity.Pelicula;
import javafx.scene.image.Image;
import java.util.List;


public class ClienteController {

    // --- VISTA PRINCIPAL (pagina_inicial.fxml) ---
    @FXML private Button btnPromoVerMas;
    @FXML private Button btnCartelera;
    @FXML private Button btnConfiteria;
    @FXML private Button btnSigmaCard;
    @FXML private Button btnCart;
    @FXML private MenuItem miCerrarSesion;
    @FXML private MenuItem miHistorial;
    @FXML private MenuButton menuPerfil;
    @FXML private StackPane promoPane;
    @FXML private ImageView imgPublicidad;
    @FXML private TextField txtBuscar;
    @FXML private javafx.scene.control.Button btnBuscar;
    @FXML private StackPane content;
    
    @FXML private ChoiceBox<String> cbCiudad;
    @FXML private Button btnSeleccionarCiudad;
    @FXML private Button btnIniciarSesion;
        @FXML private javafx.scene.control.Label lblUserName;
        @FXML private Button btnRegistrarse;

    // Footer movie cards (pagina_inicial.fxml)
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
    

    public void init(UsuarioDTO usuario) { 
        System.out.println("[INIT] init(usuario) llamado");
        this.usuario = usuario;
        // Cargar películas después de que la escena esté montada
        esperarYCargarPeliculas();
    }
    public void init(UsuarioDTO usuario, String ciudad) {
        System.out.println("[INIT] init(usuario, ciudad) llamado con ciudad: " + ciudad);
        this.usuario = usuario;
        this.ciudadSeleccionada = ciudad;
        // Cargar películas después de que la escena esté montada
        esperarYCargarPeliculas();
    }
    
    private void esperarYCargarPeliculas() {
        // Intentar buscar un elemento que debería existir para verificar si la escena está lista
        if (btnCartelera != null && btnCartelera.getScene() != null) {
            // La escena ya está lista, cargar inmediatamente
            System.out.println("[DEBUG] Escena ya está montada, cargando películas...");
            Platform.runLater(() -> cargarPeliculasInicio());
        } else if (btnCartelera != null) {
            // Esperar a que se monte la escena
            System.out.println("[DEBUG] Esperando a que la escena se monte...");
            btnCartelera.sceneProperty().addListener((obs, oldScene, newScene) -> {
                if (newScene != null) {
                    System.out.println("[DEBUG] Escena montada, cargando películas...");
                    Platform.runLater(() -> cargarPeliculasInicio());
                }
            });
        } else {
            // Como último recurso, usar Thread con delay
            System.out.println("[DEBUG] btnCartelera es null, usando Thread con delay...");
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
            cbCiudad.getItems().setAll("Bogotá", "Medellín", "Cali", "Barranquilla");
            cbCiudad.getSelectionModel().selectFirst();
        }
        // Update UI to reflect session/user state in case the session was set before load
        refreshSessionUI();
    }

    @FXML
    private void initialize() {
        System.out.println("[DEBUG] ClienteController.initialize() comenzó");
        System.out.println("[DEBUG] imgCard1 en initialize: " + imgCard1);
        System.out.println("[DEBUG] imgCard2 en initialize: " + imgCard2);
        System.out.println("[DEBUG] imgCard3 en initialize: " + imgCard3);
        System.out.println("[DEBUG] imgCard4 en initialize: " + imgCard4);
        
        if (promoPane != null && imgPublicidad != null) {
            imgPublicidad.fitWidthProperty().bind(promoPane.widthProperty());
            imgPublicidad.setFitHeight(110);
            imgPublicidad.setPreserveRatio(true);
        }
        
        if (btnSeleccionarCiudad != null) {
            btnSeleccionarCiudad.setOnAction(e -> onSeleccionarCiudad());
        }

    if (btnCartelera!= null) btnCartelera.setOnAction(e -> mostrarCartelera());
        if (btnConfiteria != null) btnConfiteria.setOnAction(e -> System.out.println("Ir a Confitería (" + safeCiudad() + ")"));
        if (miCerrarSesion != null) miCerrarSesion.setOnAction(e -> onLogout());
        
        if (miHistorial != null) miHistorial.setOnAction(e -> onVerHistorial()); // Llama al método corregido.

    // Disable profile menu when not logged in
    updateMenuPerfilState();

        if (btnIniciarSesion != null) {
            btnIniciarSesion.setOnAction(e -> onIniciarSesion());
        }

        // Ensure UI reflects current session (may have been set before this controller loaded)
        refreshSessionUI();
        if (btnRegistrarse != null) {
            btnRegistrarse.setOnAction(e -> onRegistrarse());
            // disable register when already logged in
            btnRegistrarse.setDisable(Session.isLoggedIn());
        }

        if (txtBuscar != null) {
            txtBuscar.setOnKeyPressed(ev -> {
                if (ev.getCode() == KeyCode.ENTER) {
                    doSearch(txtBuscar.getText());
                }
            });
            if (btnBuscar != null) btnBuscar.setOnAction(e -> doSearch(txtBuscar.getText()));
        }

        // Mostrar/ocultar la publicidad dependiendo de si el contenedor 'content' tiene sub-vistas
        if (content != null) {
            content.getChildren().addListener((ListChangeListener<Node>) ch -> {
                updatePublicidadVisibility();
            });
            // initial state
            updatePublicidadVisibility();
        }

        // Las películas se cargarán cuando se llame init() o init(usuario, ciudad)
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

    public void setCoordinador(ControladorControlador c) { this.coordinador = c; }

    private void onIniciarSesion() {
        System.out.println("[DEBUG] onIniciarSesion invoked");
        // if already logged in, perform logout; otherwise show login
        if (Session.isLoggedIn()) {
            // ask for confirmation before logging out
            javafx.scene.control.Alert a = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.CONFIRMATION);
            a.setTitle("Cerrar sesión");
            a.setHeaderText("¿Desea cerrar sesión?");
            a.setContentText("Salir de la cuenta " + (Session.getCurrent() != null ? Session.getCurrent().getEmail() : "") );
            var opt = a.showAndWait();
            if (opt.isPresent() && opt.get() == javafx.scene.control.ButtonType.OK) {
                onLogout();
            }
            return;
        }
        if (coordinador != null) coordinador.mostrarLogin();
    }

    private void onRegistrarse() {
        System.out.println("[DEBUG] onRegistrarse invoked");
        if (Session.isLoggedIn()) {
            // already logged in — don't allow registering a new account
            javafx.scene.control.Alert a = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
            a.setTitle("Ya has iniciado sesión");
            a.setHeaderText(null);
            a.setContentText("Ya has iniciado sesión. Cierra sesión si deseas registrar una nueva cuenta.");
            a.showAndWait();
            return;
        }
        if (coordinador != null) coordinador.mostrarRegistro();
    }


    private String safeCiudad() {
        return ciudadSeleccionada != null ? ciudadSeleccionada : "sin ciudad";
    }

    private void onLogout() {
        System.out.println("Cerrar sesión de: " + (usuario != null ? usuario.getEmail() : "desconocido"));
        // clear application session and update UI
        Session.clear();
        this.usuario = null;
        if (lblUserName != null) { lblUserName.setText(""); lblUserName.setVisible(false); }
        if (btnIniciarSesion != null) { btnIniciarSesion.setVisible(true); btnIniciarSesion.setText("Iniciar sesión"); }
        if (btnRegistrarse != null) btnRegistrarse.setDisable(false);
        // update menu after logout
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

    /** Update the topbar controls to reflect the current session. */
    private void refreshSessionUI() {
        try {
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
                if (btnRegistrarse != null) btnRegistrarse.setDisable(true);
            } else {
                if (lblUserName != null) { lblUserName.setText(""); lblUserName.setVisible(false); }
                if (btnIniciarSesion != null) { btnIniciarSesion.setVisible(true); btnIniciarSesion.setText("Iniciar sesión"); }
                if (btnRegistrarse != null) btnRegistrarse.setDisable(false);
            }
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    /** Enable/disable the profile MenuButton depending on session state. */
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
    
    @FXML private void onPromoVerMas() { System.out.println("Promoción → Ver más (" + safeCiudad() + ")"); }
    @FXML private void onCard1(){ System.out.println("Card 1 → Ver más (" + safeCiudad() + ")"); }
    @FXML private void onCard2(){ System.out.println("Card 2 → Ver más (" + safeCiudad() + ")"); }
    @FXML private void onCard3(){ System.out.println("Card 3 → Ver más (" + safeCiudad() + ")"); }
    @FXML private void onCard4(){ System.out.println("Card 4 → Ver más (" + safeCiudad() + ")"); }


    private void onSeleccionarCiudad() {
        String ciudad = (cbCiudad != null) ? cbCiudad.getValue() : null;
        if (ciudad == null || ciudad.isBlank()) return;

        try {
            // Load the main client home screen so both flows land on the same initial UI
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/sigmacine/ui/views/pagina_inicial.fxml"));
            Parent root = loader.load();
            ClienteController controller = loader.getController();
            controller.init(this.usuario, ciudad);

            Stage stage = (Stage) btnSeleccionarCiudad.getScene().getWindow();
            stage.setTitle("Sigma Cine - Cliente (" + ciudad + ")");
            javafx.scene.Scene current = stage.getScene();
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
            // pass current session info so the results view can return to the same client home
            controller.setCoordinador(this.coordinador);
            controller.setUsuario(this.usuario);
            controller.setResultados(resultados, texto);

            javafx.stage.Stage stage = (javafx.stage.Stage) content.getScene().getWindow();
            javafx.scene.Scene current = stage.getScene();
            double w = current != null ? current.getWidth() : 900;
            double h = current != null ? current.getHeight() : 600;
            stage.setScene(new Scene(root, w > 0 ? w : 900, h > 0 ? h : 600));
            stage.setTitle("Resultados de búsqueda");
            stage.setMaximized(true);

        } catch (Exception ex) {
            System.err.println("Error cargando resultados_busqueda.fxml: " + ex.getMessage());
            ex.printStackTrace();
            throw new RuntimeException("Error cargando resultados_busqueda.fxml", ex);
        }
    }

    @FXML
    private void onVerHistorial() {
        System.out.println("Navegando a Historial de Compras.");
        if (!Session.isLoggedIn()) {
            javafx.scene.control.Alert a = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
            a.setTitle("Acceso denegado");
            a.setHeaderText(null);
            a.setContentText("Debes iniciar sesión para ver tu historial de compras.");
            a.showAndWait();
            return;
        }
        try {
            DatabaseConfig dbConfig = new DatabaseConfig();
            // Asume que VerHistorialService requiere un repositorio de Usuario para buscar el historial.
            var usuarioRepo = new UsuarioRepositoryJdbc(dbConfig);
            var historialService = new VerHistorialService(usuarioRepo);
            
            // Carga el FXML (ruta verificada y correcta)
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/sigmacine/ui/views/verCompras.fxml"));
            
            VerHistorialController historialController = new VerHistorialController(historialService);
            historialController.setClienteController(this);

            if (this.usuario != null) {
                historialController.setUsuarioEmail(this.usuario.getEmail());
            }

            loader.setController(historialController);
            
            Parent historialView = loader.load();
            
            // 3. Muestra la vista
            content.getChildren().setAll(historialView);
            
        } catch (Exception ex) {
            System.err.println("Error cargando HistorialDeCompras.fxml: " + ex.getMessage());
            ex.printStackTrace();
            content.getChildren().setAll(new Label("Error cargando Historial: " + ex.getMessage()));
        }
    }
    
    public void mostrarCartelera() {
        System.out.println("Volviendo a Cartelera (Contenido a pantalla completa).");
        try {
            java.net.URL url = getClass().getResource("/sigmacine/ui/views/cartelera.fxml");
            Parent carteleraView;
            FXMLLoader loader = null;
            if (url != null) {
                loader = new FXMLLoader(url);
                carteleraView = loader.load();
                // allow controller wiring if present
                try {
                    var ctrl = loader.getController();
                    try {
                        java.lang.reflect.Method m = ctrl.getClass().getMethod("init", sigmacine.aplicacion.data.UsuarioDTO.class);
                        if (m != null) m.invoke(ctrl, this.usuario);
                    } catch (NoSuchMethodException ignore) {}
                    try {
                        java.lang.reflect.Method su = ctrl.getClass().getMethod("setUsuario", sigmacine.aplicacion.data.UsuarioDTO.class);
                        if (su != null) su.invoke(ctrl, this.usuario);
                    } catch (NoSuchMethodException ignore) {}
                    try {
                        java.lang.reflect.Method sc = ctrl.getClass().getMethod("setCoordinador", sigmacine.ui.controller.ControladorControlador.class);
                        if (sc != null) sc.invoke(ctrl, this.coordinador);
                    } catch (NoSuchMethodException ignore) {}
                    try {
                        java.lang.reflect.Method rf = ctrl.getClass().getMethod("refreshSessionUI");
                        if (rf != null) rf.invoke(ctrl);
                    } catch (NoSuchMethodException ignore) {}
                } catch (Exception e) { e.printStackTrace(); }
            } else {
                // fallback: reuse the previous detail view but prevent empty load — call doSearch to show results instead
                System.out.println("cartelera.fxml not found, showing results instead");
                cargarPeliculasInicio();
                return;
            }

            // Si el controlador de contenidoCartelera.fxml necesita inicialización, invocamos init(usuario)
            try {
                var ctrl = loader.getController();
                // try init(UsuarioDTO)
                try {
                    java.lang.reflect.Method m = ctrl.getClass().getMethod("init", sigmacine.aplicacion.data.UsuarioDTO.class);
                    if (m != null) m.invoke(ctrl, this.usuario);
                } catch (NoSuchMethodException ignore) {}

                // fallback: try setUsuario(UsuarioDTO)
                try {
                    java.lang.reflect.Method su = ctrl.getClass().getMethod("setUsuario", sigmacine.aplicacion.data.UsuarioDTO.class);
                    if (su != null) su.invoke(ctrl, this.usuario);
                } catch (NoSuchMethodException ignore) {}

                // also try to set the coordinator so the controller can navigate back while preserving session
                try {
                    java.lang.reflect.Method sc = ctrl.getClass().getMethod("setCoordinador", sigmacine.ui.controller.ControladorControlador.class);
                    if (sc != null) sc.invoke(ctrl, this.coordinador);
                } catch (NoSuchMethodException ignore) {}

                // If controller exposes refreshSessionUI(), call it so the topbar updates immediately
                try {
                    java.lang.reflect.Method rf = ctrl.getClass().getMethod("refreshSessionUI");
                    if (rf != null) rf.invoke(ctrl);
                } catch (NoSuchMethodException ignore) {}

            } catch (Exception e) {
                // log but continue to show the view
                e.printStackTrace();
            }

            // Reemplazamos la Scene entera para que ocupe toda la ventana
            Stage stage = (Stage) content.getScene().getWindow();
            javafx.scene.Scene current = stage.getScene();
            double w = current != null ? current.getWidth() : 900;
            double h = current != null ? current.getHeight() : 600;
            stage.setScene(new Scene(carteleraView, w > 0 ? w : 900, h > 0 ? h : 600));
            stage.setTitle("Sigma Cine - Cartelera");
            stage.setMaximized(true);
        } catch (Exception e) {
            content.getChildren().setAll(new Label("Error: No se pudo cargar la vista de cartelera."));
            e.printStackTrace();
        }
    }

    /**
     * Carga las películas desde la BD y las muestra en las 4 cards del footer.
     * Llena los pósters y títulos de las películas.
     */
    private void cargarPeliculasInicio() {
        System.out.println("[DEBUG] cargarPeliculasInicio() invocado");
        System.out.println("[DEBUG] footerGrid = " + footerGrid);
        System.out.println("[DEBUG] imgCard1 = " + imgCard1);
        
        try {
            DatabaseConfig db = new DatabaseConfig();
            PeliculaRepositoryJdbc repo = new PeliculaRepositoryJdbc(db);
            
            // Buscar todas las películas (o filtrar por estado "En Cartelera")
            List<Pelicula> peliculas = repo.buscarPorTitulo(""); // buscar todas
            
            System.out.println("[DEBUG] Películas encontradas: " + (peliculas != null ? peliculas.size() : 0));
            
            // Si no hay películas, salir
            if (peliculas == null || peliculas.isEmpty()) {
                System.out.println("No se encontraron películas para mostrar en la página inicial.");
                return;
            }
            
            // Si los campos @FXML están null, intentar ubicar el GridPane desde la raíz de la escena
            if (footerGrid == null) {
                footerGrid = localizarFooterGridDesdeRoot();
                System.out.println("[DEBUG] localizarFooterGridDesdeRoot => " + footerGrid);
            }
            // Si aún no lo tenemos, como fallback intentar lookup
            if (footerGrid == null) {
                System.out.println("[DEBUG] Campos @FXML null, intentando buscar con lookup...");
                buscarYCargarConLookup(peliculas);
                return;
            }
            
            // Independientemente de que existan placeholders en el FXML, renderizamos dinámicamente
            // para controlar tamaño y centrado con precisión.
            System.out.println("[DEBUG] Renderizando dinámicamente el footer (forzado)");
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
        System.out.println("[DEBUG] buscarYCargarConLookup() - Buscando elementos con lookup...");
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
    
    private ImageView buscarImageView(String fxId) {
        try {
            // Buscar en todos los campos @FXML conocidos
            if (btnCartelera != null && btnCartelera.getScene() != null) {
                return (ImageView) btnCartelera.getScene().lookup(fxId);
            }
            if (content != null && content.getScene() != null) {
                return (ImageView) content.getScene().lookup(fxId);
            }
        } catch (Exception ex) {
            System.err.println("Error buscando ImageView " + fxId + ": " + ex.getMessage());
        }
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
        } catch (Exception ex) {
            System.err.println("Error buscando Label " + fxId + ": " + ex.getMessage());
        }
        return null;
    }
    
    private void buscarYCargarEnGrid(List<Pelicula> peliculas) {
        System.out.println("[DEBUG] Buscando elementos en footerGrid...");
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
                        System.out.println("[DEBUG] Cargando en columna " + colIndex + ": " + pelicula.getTitulo());
                        
                        if (posterUrl != null && !posterUrl.isBlank()) {
                            Image posterImage = resolveImage(posterUrl);
                            if (posterImage != null) {
                                img.setImage(posterImage);
                                System.out.println("✓ Póster cargado para: " + pelicula.getTitulo());
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
                        System.out.println("[DEBUG] Título cargado en columna " + colIndex + ": " + pelicula.getTitulo());
                    }
                }
            }
        } catch (Exception ex) {
            System.err.println("Error buscando elementos en footerGrid: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
    
    /**
     * Carga el póster y título de una película en una card (ImageView + Label).
     */
    private void cargarCard(ImageView img, Label lbl, Pelicula pelicula) {
        if (pelicula == null) return;
        
        // Poner el título
        if (lbl != null) {
            lbl.setText(pelicula.getTitulo() != null ? pelicula.getTitulo() : "Sin título");
        }
        
        // Cargar el póster
        if (img != null) {
            String posterUrl = pelicula.getPosterUrl();
            System.out.println("Cargando card para: " + pelicula.getTitulo() + " con URL: " + posterUrl);
            
            if (posterUrl != null && !posterUrl.isBlank()) {
                Image posterImage = resolveImage(posterUrl);
                if (posterImage != null) {
                    img.setImage(posterImage);
                    System.out.println("✓ Póster cargado exitosamente para: " + pelicula.getTitulo());
                } else {
                    System.err.println("✗ No se pudo cargar el póster para: " + pelicula.getTitulo());
                }
            }
        }
    }
    
    /**
     * Resuelve la ruta de una imagen probando varias estrategias:
     * 1. Si es URL http/https, la carga directamente
     * 2. Si está en src\main\resources\Images\, extrae solo el nombre del archivo
     * 3. Prueba cargar desde /Images/nombre
     * 4. Prueba como archivo local si existe
     */
    private Image resolveImage(String ref) {
        if (ref == null || ref.isBlank()) return null;
        
        try {
            String lower = ref.toLowerCase();
            
            // 1. URLs externas
            if (lower.startsWith("http://") || lower.startsWith("https://") || lower.startsWith("file:/")) {
                return new Image(ref, true);
            }
            
            // 2. Si contiene la ruta completa "src\main\resources\Images\", extraer solo el nombre
            if (ref.contains("src\\main\\resources\\Images\\") || ref.contains("src/main/resources/Images/")) {
                String fileName = ref.substring(ref.lastIndexOf("\\") + 1);
                if (fileName.isEmpty()) fileName = ref.substring(ref.lastIndexOf("/") + 1);
                
                System.out.println("  → Extrayendo nombre de archivo: " + fileName);
                java.net.URL res = getClass().getResource("/Images/" + fileName);
                if (res != null) {
                    System.out.println("  → Encontrado en: " + res.toExternalForm());
                    return new Image(res.toExternalForm(), false);
                }
            }
            
            // 3. Probar como recurso directo /Images/...
            java.net.URL res = getClass().getResource("/Images/" + ref);
            if (res != null) return new Image(res.toExternalForm(), false);
            
            // 4. Probar como archivo local
            java.io.File f = new java.io.File(ref);
            if (f.exists()) return new Image(f.toURI().toString(), false);
            
            // 5. Probar con / al inicio
            res = getClass().getResource(ref.startsWith("/") ? ref : ("/" + ref));
            if (res != null) return new Image(res.toExternalForm(), false);
            
        } catch (Exception ex) {
            System.err.println("  → Error resolviendo imagen: " + ex.getMessage());
        }
        
        return null;
    }
}