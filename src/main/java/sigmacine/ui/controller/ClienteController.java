package sigmacine.ui.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.Node;
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

import sigmacine.aplicacion.service.VerHistorialService;
import sigmacine.infraestructura.configDataBase.DatabaseConfig;
import sigmacine.infraestructura.persistencia.jdbc.PeliculaRepositoryJdbc;
import sigmacine.infraestructura.persistencia.jdbc.UsuarioRepositoryJdbc;
import sigmacine.aplicacion.session.Session;

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
    @FXML private Label lblPromo;
    @FXML private TextField txtBuscar;
    @FXML private Button btnBuscar;
    @FXML private StackPane content;

    @FXML private ChoiceBox<String> cbCiudad;
    @FXML private Button btnSeleccionarCiudad;
    @FXML private Button btnIniciarSesion;
    @FXML private Button btnRegistrarse;

    private UsuarioDTO usuario;
    private String ciudadSeleccionada;
    private ControladorControlador coordinador;

    public void setCoordinador(ControladorControlador c) { this.coordinador = c; }

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

        if (btnCartelera != null) btnCartelera.setOnAction(e -> mostrarCartelera());
        if (btnConfiteria != null) btnConfiteria.setOnAction(e -> System.out.println("Ir a Confitería (" + safeCiudad() + ")"));
        if (miCerrarSesion != null) miCerrarSesion.setOnAction(e -> onLogout());
        if (miHistorial != null) miHistorial.setOnAction(e -> onVerHistorial());

        if (btnIniciarSesion != null) {
            btnIniciarSesion.setOnAction(e -> onIniciarSesion());
            if (Session.isLoggedIn()) {
                var u = Session.getCurrent();
                btnIniciarSesion.setText(u != null && u.getEmail() != null ? u.getEmail() : "Cerrar sesión");
                this.usuario = Session.getCurrent();
            } else {
                btnIniciarSesion.setText("Iniciar sesión");
            }
        }
        if (btnRegistrarse != null) {
            btnRegistrarse.setOnAction(e -> onRegistrarse());
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

        // Si aún tenías esto para pruebas, puedes comentarlo si te estorba el flujo:
        // mostrarAsientosAhora();
    }

    // ===== Helper opcional: saber si un Node vive en la misma Scene que este content
    public boolean isSameScene(Node n) {
        return content != null && n != null && content.getScene() == n.getScene();
    }

    private void onIniciarSesion() {
        System.out.println("[DEBUG] onIniciarSesion invoked");
        if (Session.isLoggedIn()) {
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
            javafx.scene.control.Alert a = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
            a.setTitle("Ya has iniciado sesión");
            a.setHeaderText(null);
            a.setContentText("Ya has iniciado sesión. Cierra sesión si deseas registrar una nueva cuenta.");
            a.showAndWait();
            return;
        }
        if (coordinador != null) coordinador.mostrarRegistro();
    }

    private String safeCiudad() { return ciudadSeleccionada != null ? ciudadSeleccionada : "sin ciudad"; }

    private void onLogout() {
        System.out.println("Cerrar sesión de: " + (usuario != null ? usuario.getEmail() : "desconocido"));
        Session.clear();
        this.usuario = null;
        if (btnIniciarSesion != null) btnIniciarSesion.setText("Iniciar sesión");
        if (btnRegistrarse != null) btnRegistrarse.setDisable(false);
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
            controller.setCoordinador(this.coordinador);
            controller.setUsuario(this.usuario);
            controller.setResultados(resultados, texto);

            Stage stage = (Stage) content.getScene().getWindow();
            Scene current = stage.getScene();
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

    /** Cartelera a pantalla completa (reemplaza Scene): NO seteamos host aquí. */
    public void mostrarCartelera() {
        System.out.println("Volviendo a Cartelera (Contenido a pantalla completa).");
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/sigmacine/ui/views/contenidoCartelera.fxml"));
            Parent carteleraView = loader.load();

            var ctrlObj = loader.getController();
            if (ctrlObj instanceof ContenidoCarteleraController c) {
                // ✨ Importante: NO setHost(this) cuando reemplazas la Scene
                c.setCoordinador(this.coordinador);
                c.setUsuario(this.usuario);
            } else {
                // fallback reflectivo
                try {
                    java.lang.reflect.Method m = ctrlObj.getClass().getMethod("init", sigmacine.aplicacion.data.UsuarioDTO.class);
                    if (m != null) m.invoke(ctrlObj, this.usuario);
                } catch (NoSuchMethodException ignore) {}
                try {
                    java.lang.reflect.Method su = ctrlObj.getClass().getMethod("setUsuario", sigmacine.aplicacion.data.UsuarioDTO.class);
                    if (su != null) su.invoke(ctrlObj, this.usuario);
                } catch (NoSuchMethodException ignore) {}
                try {
                    java.lang.reflect.Method sc = ctrlObj.getClass().getMethod("setCoordinador", sigmacine.ui.controller.ControladorControlador.class);
                    if (sc != null) sc.invoke(ctrlObj, this.coordinador);
                } catch (NoSuchMethodException ignore) {}
            }

            Stage stage = (Stage) content.getScene().getWindow();
            Scene current = stage.getScene();
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

    private void togglePromo(boolean show) {
        if (promoPane != null){
            promoPane.setVisible(show);
            promoPane.setManaged(show);
        }
        if (imgPublicidad != null){
            imgPublicidad.setVisible(show);
            imgPublicidad.setManaged(show);
        }
        if (lblPromo != null){
            lblPromo.setVisible(show);
            lblPromo.setManaged(show);
        }
        if (btnPromoVerMas != null){
            btnPromoVerMas.setVisible(show);
            btnPromoVerMas.setManaged(show);
        }
    }

    // Solo pruebas locales (puedes comentarlo):
    private void mostrarAsientosAhora() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/sigmacine/ui/views/asientos.fxml"));
            Parent pane = loader.load();

            sigmacine.ui.controller.AsientosController ctrl = loader.getController();
            var ocup  = java.util.Set.of("B3","B4","C7","E2","F8");
            var acces = java.util.Set.of("E3","E4","E5","E6");
            ctrl.setFuncion("Los 4 Fantásticos", "1:10 pm", ocup, acces);

            content.getChildren().clear();
            content.getChildren().add(pane);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void volverHomeContenido() {
        togglePromo(true);
    }

    /** Flujo Cartelera -> Asientos embebido en `content` (cuando SÍ estás embebido). */
    public void mostrarAsientos(String titulo, String hora,
                                java.util.Set<String> ocupados,
                                java.util.Set<String> accesibles) {
        try {
            togglePromo(false);

            var url = getClass().getResource("/sigmacine/ui/views/asientos_contenido.fxml");
            if (url == null) {
                url = getClass().getResource("/sigmacine/ui/views/asientos.fxml");
            }
            FXMLLoader loader = new FXMLLoader(java.util.Objects.requireNonNull(url, "No se encontró la vista de asientos"));
            Parent pane = loader.load();

            sigmacine.ui.controller.AsientosController ctrl = loader.getController();
            ctrl.setFuncion(titulo, hora, ocupados, accesibles);

            content.getChildren().setAll(pane);
        } catch (Exception e) {
            e.printStackTrace();
            content.getChildren().setAll(new Label("Error cargando Asientos: " + e.getMessage()));
        }
    }
}
