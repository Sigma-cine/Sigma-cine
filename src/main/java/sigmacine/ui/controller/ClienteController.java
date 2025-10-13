package sigmacine.ui.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import sigmacine.aplicacion.data.UsuarioDTO;
import javafx.scene.image.ImageView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
// Dependencias necesarias para instanciación manual
import sigmacine.aplicacion.service.VerHistorialService;
import sigmacine.infraestructura.configDataBase.DatabaseConfig;
import sigmacine.infraestructura.persistencia.jdbc.PeliculaRepositoryJdbc;
import sigmacine.infraestructura.persistencia.jdbc.UsuarioRepositoryJdbc; 
import sigmacine.ui.controller.ControladorControlador;


public class ClienteController {

    // --- VISTA PRINCIPAL (cliente_home.fxml) ---
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
    @FXML private javafx.scene.control.Button btnBuscar;
    @FXML private StackPane content;
    
    @FXML private ChoiceBox<String> cbCiudad;
    @FXML private Button btnSeleccionarCiudad;
    @FXML private Button btnIniciarSesion;
    @FXML private Button btnRegistrarse;

    private UsuarioDTO usuario;
    private String ciudadSeleccionada;
    private ControladorControlador coordinador;
    

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

    if (btnCartelera!= null) btnCartelera.setOnAction(e -> mostrarCartelera());
        if (btnConfiteria != null) btnConfiteria.setOnAction(e -> System.out.println("Ir a Confitería (" + safeCiudad() + ")"));
        if (miCerrarSesion != null) miCerrarSesion.setOnAction(e -> onLogout());
        
        if (miHistorial != null) miHistorial.setOnAction(e -> onVerHistorial()); // Llama al método corregido.

    if (btnIniciarSesion != null) btnIniciarSesion.setOnAction(e -> onIniciarSesion());
    if (btnRegistrarse != null) btnRegistrarse.setOnAction(e -> onRegistrarse());

        if (txtBuscar != null) {
            txtBuscar.setOnKeyPressed(ev -> {
                if (ev.getCode() == KeyCode.ENTER) {
                    doSearch(txtBuscar.getText());
                }
            });
            if (btnBuscar != null) btnBuscar.setOnAction(e -> doSearch(txtBuscar.getText()));
        }
    }

    public void setCoordinador(ControladorControlador c) { this.coordinador = c; }

    private void onIniciarSesion() {
        System.out.println("[DEBUG] onIniciarSesion invoked");
        if (coordinador != null) coordinador.mostrarLogin();
    }

    private void onRegistrarse() {
        System.out.println("[DEBUG] onRegistrarse invoked");
        if (coordinador != null) coordinador.mostrarRegistro();
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
            // Load the main client home screen so both flows land on the same initial UI
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/sigmacine/ui/views/cliente_home.fxml"));
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
            throw new RuntimeException("Error cargando cliente_home.fxml", ex);
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
        try {
            DatabaseConfig dbConfig = new DatabaseConfig();
            // Asume que VerHistorialService requiere un repositorio de Usuario para buscar el historial.
            var usuarioRepo = new UsuarioRepositoryJdbc(dbConfig);
            var historialService = new VerHistorialService(usuarioRepo);
            
            // Carga el FXML (ruta verificada y correcta)
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/sigmacine/ui/views/VerCompras.fxml"));
            
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/sigmacine/ui/views/contenidoCartelera.fxml"));
            Parent carteleraView = loader.load();

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
}