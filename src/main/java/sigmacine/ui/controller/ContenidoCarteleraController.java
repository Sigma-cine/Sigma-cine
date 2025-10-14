package sigmacine.ui.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.stage.Modality;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import sigmacine.aplicacion.data.UsuarioDTO;
import sigmacine.aplicacion.data.FuncionDisponibleDTO;
import sigmacine.dominio.entity.Pelicula;
import sigmacine.dominio.repository.FuncionRepository;
import sigmacine.infraestructura.configDataBase.DatabaseConfig;
import sigmacine.aplicacion.session.Session;
import sigmacine.infraestructura.persistencia.jdbc.FuncionRepositoryJdbc;
import sigmacine.aplicacion.session.Session;
import sigmacine.infraestructura.configDataBase.DatabaseConfig;
import sigmacine.infraestructura.persistencia.jdbc.UsuarioRepositoryJdbc;
import sigmacine.aplicacion.service.VerHistorialService;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.Objects;
import java.util.Set;
// (duplicate import removed)
import sigmacine.ui.controller.ControladorControlador;

public class ContenidoCarteleraController {

    @FXML private Button btnCarteleraTop;
    @FXML private Button btnBack;

    // Session-related topbar controls (copied from pagina_inicial.fxml)
    @FXML private MenuButton menuPerfil;
    @FXML private Button btnIniciarSesion;
    @FXML private Button btnRegistrarse;
    @FXML private Label lblUserName;
    @FXML private MenuItem miCerrarSesion;
    @FXML private MenuItem miHistorial;
    @FXML private TextArea txtSinopsis;
    @FXML private Label lblSinopsisTitulo;
    @FXML private Label lblTituloPelicula;
    @FXML private VBox panelFunciones;
    @FXML private Button btnComprar;
    @FXML private Label lblGenero, lblClasificacion, lblDuracion, lblDirector, lblReparto;
    @FXML private GridPane gridSala;
    @FXML private Label lblResumen, lblTitulo, lblHoraPill;
    @FXML private ListView<String> lvFunciones;
    @FXML private ImageView imgPoster;
    @FXML private Button btnContinuar;

    @FXML private StackPane trailerContainer;
    @FXML private ScrollPane spCenter;
    @FXML private VBox detalleRoot;

    private Pelicula pelicula;
    private UsuarioDTO usuario;
    private ControladorControlador coordinador;
    // optional back navigation state (currently not used)
    // private List<Pelicula> backPeliculas;
    // private String backTexto;
    // back navigation placeholders (reserved for future use)

    private ClienteController host;
    public void setHost(ClienteController host) { this.host = host; }


    @FXML
    private void initialize() {
        if (trailerContainer != null && trailerContainer.getChildren().isEmpty()) {
            trailerContainer.setMouseTransparent(true);
            trailerContainer.setPickOnBounds(false);
        }
        if (spCenter != null) spCenter.setPannable(false);

        if (btnComprar != null) {
            btnComprar.setDisable(false);
            btnComprar.setMouseTransparent(false);
            btnComprar.setPickOnBounds(true);
            btnComprar.setFocusTraversable(true);
            btnComprar.setViewOrder(-1000);
            btnComprar.toFront();
            if (btnComprar.getParent() != null) btnComprar.getParent().toFront();

            btnComprar.setOnAction(e -> onComprarTickets());
        }
        // Wire historial menu action (open as modal from detail page)
        if (miHistorial != null) {
            miHistorial.setOnAction(e -> onVerHistorial());
        }
        if (btnRegistrarse != null) {
            btnRegistrarse.setOnAction(e -> onRegistrarse());
        }
        // session UI setup
        try { refreshSessionUI(); } catch (Exception ignore) {}
        if (btnIniciarSesion != null) btnIniciarSesion.setOnAction(e -> {
            try {
                // Prefer to delegate to the app coordinator so AuthFacade is injected
                if (this.coordinador != null) {
                    this.coordinador.mostrarLogin();
                    refreshSessionUI();
                    return;
                }
                // Try global coordinator instance if one was registered
                try {
                    ControladorControlador global = ControladorControlador.getInstance();
                    if (global != null) {
                        global.mostrarLogin();
                        refreshSessionUI();
                        return;
                    }
                } catch (Throwable ignore) {}

                // Fallback: load login.fxml manually but try to set AuthFacade if available
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/sigmacine/ui/views/login.fxml"));
                Parent root = loader.load();
                Object ctrl = loader.getController();
                Stage dialog = new Stage();
                dialog.initModality(Modality.APPLICATION_MODAL);
                dialog.initOwner(btnIniciarSesion.getScene().getWindow());
                if (ctrl instanceof LoginController) {
                    LoginController lc = (LoginController) ctrl;
                    try {
                        ControladorControlador global = ControladorControlador.getInstance();
                        if (global != null) {
                            lc.setCoordinador(global);
                            // set AuthFacade if available via getter
                            try {
                                sigmacine.aplicacion.facade.AuthFacade af = global.getAuthFacade();
                                if (af != null) lc.setAuthFacade(af);
                            } catch (Throwable ignore) {}
                        }
                    } catch (Throwable ignore) {}

                    // Ensure that after successful login we don't navigate away from the
                    // detail view: instead close the dialog and refresh the session UI.
                    lc.setOnSuccess(() -> {
                        try { dialog.close(); } catch (Exception ignore) {}
                        try { refreshSessionUI(); } catch (Exception ignore) {}
                    });
                }
                dialog.setScene(new Scene(root));
                dialog.showAndWait();
            } catch (Exception ex) { ex.printStackTrace(); }
        });
    if (miCerrarSesion != null) miCerrarSesion.setOnAction(e -> { sigmacine.aplicacion.session.Session.clear(); refreshSessionUI(); });
    }

    // duplicate onBrandClick/onVerHistorial/refreshSessionUI removed

    @FXML
    private void onBrandClick() {
        try {
            Stage stage = (Stage) (btnBack != null ? btnBack.getScene().getWindow() : btnCarteleraTop.getScene().getWindow());
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/sigmacine/ui/views/pagina_inicial.fxml"));
            Parent root = loader.load();
            Object ctrl = loader.getController();
            if (ctrl instanceof ClienteController) {
                ClienteController c = (ClienteController) ctrl;
                c.setCoordinador(this.coordinador);
                c.init(this.usuario);
            }
            Scene current = stage.getScene();
            double w = current != null ? current.getWidth() : 1000;
            double h = current != null ? current.getHeight() : 600;
            stage.setScene(new Scene(root, w, h));
            stage.setTitle("Sigma Cine");
            stage.setMaximized(true);
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    private void onVerHistorial() {
        // Require login like in ClienteController
    if (!sigmacine.aplicacion.session.Session.isLoggedIn()) {
            javafx.scene.control.Alert a = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
            a.setTitle("Acceso denegado");
            a.setHeaderText(null);
            a.setContentText("Debes iniciar sesión para ver tu historial de compras.");
            a.showAndWait();
            return;
        }
        try {
            // Build service and controller
            DatabaseConfig dbConfig = new DatabaseConfig();
            UsuarioRepositoryJdbc usuarioRepo = new UsuarioRepositoryJdbc(dbConfig);
            VerHistorialService historialService = new VerHistorialService(usuarioRepo);

            // Load full-screen history view (not a modal)
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/sigmacine/ui/views/verCompras.fxml"));
            VerHistorialController historialController = new VerHistorialController(historialService);
            // Inject email if available
            try {
                if (this.usuario != null && this.usuario.getEmail() != null) {
                    historialController.setUsuarioEmail(this.usuario.getEmail());
                } else {
                    // fallback to session current user if available
                    sigmacine.aplicacion.data.UsuarioDTO cur = sigmacine.aplicacion.session.Session.getCurrent();
                    if (cur != null && cur.getEmail() != null) historialController.setUsuarioEmail(cur.getEmail());
                }
            } catch (Exception ignore) {}
            loader.setController(historialController);

            Parent root = loader.load();
            // Replace current window scene to navigate fully
            Stage stage = null;
            try { stage = (Stage) (btnBack != null ? btnBack.getScene().getWindow() : (menuPerfil != null ? menuPerfil.getScene().getWindow() : null)); } catch (Exception ignore) {}
            if (stage != null) {
                Scene current = stage.getScene();
                double w = current != null ? current.getWidth() : 1000;
                double h = current != null ? current.getHeight() : 700;
                stage.setScene(new Scene(root, w, h));
                stage.setTitle("Historial de compras");
                stage.setMaximized(true);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void refreshSessionUI() {
        try {
            var current = sigmacine.aplicacion.session.Session.getCurrent();
            boolean logged = sigmacine.aplicacion.session.Session.isLoggedIn();
            if (btnIniciarSesion != null) { btnIniciarSesion.setVisible(!logged); btnIniciarSesion.setManaged(!logged); }
            if (btnRegistrarse != null) { btnRegistrarse.setVisible(!logged); btnRegistrarse.setManaged(!logged); }
            if (lblUserName != null) { lblUserName.setVisible(logged); lblUserName.setManaged(logged); lblUserName.setText(logged && current != null ? current.getNombre() : ""); }
            if (menuPerfil != null) { menuPerfil.setVisible(logged); menuPerfil.setManaged(logged); }
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    @FXML
    private void onCartelera() {
        try {
            // Navegar a la vista de cartelera completa
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/sigmacine/ui/views/cartelera.fxml"));
            Parent root = loader.load();
            try {
                Object ctrl = loader.getController();
                if (ctrl instanceof sigmacine.ui.controller.CarteleraController) {
                    sigmacine.ui.controller.CarteleraController c = (sigmacine.ui.controller.CarteleraController) ctrl;
                    c.setCoordinador(this.coordinador);
                    c.setUsuario(this.usuario);
                }
            } catch (Exception ignore) {}
            Stage stage = (Stage) btnCarteleraTop.getScene().getWindow();
            Scene current = stage.getScene();
            double w = current != null ? current.getWidth() : 900;
            double h = current != null ? current.getHeight() : 600;
            stage.setScene(new Scene(root, w > 0 ? w : 900, h > 0 ? h : 600));
            stage.setTitle("Sigma Cine - Cartelera");
            stage.setMaximized(true);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void onRegistrarse() {
        try {
            // si ya hay sesión, no permitir registrar
            if (sigmacine.aplicacion.session.Session.isLoggedIn()) {
                javafx.scene.control.Alert a = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
                a.setTitle("Ya has iniciado sesión");
                a.setHeaderText(null);
                a.setContentText("Cierra sesión si deseas registrar una nueva cuenta.");
                a.showAndWait();
                return;
            }
            if (this.coordinador != null) {
                this.coordinador.mostrarRegistro();
                return;
            }
            // intentar usar coordinador global
            try {
                ControladorControlador global = ControladorControlador.getInstance();
                if (global != null) { global.mostrarRegistro(); return; }
            } catch (Throwable ignore) {}

            // Fallback: no hay coordinador — informar al usuario
            javafx.scene.control.Alert a = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
            a.setTitle("Registro");
            a.setHeaderText(null);
            a.setContentText("No fue posible abrir el registro en este contexto.");
            a.showAndWait();
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    @FXML
    private void onVolver() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/sigmacine/ui/views/pagina_inicial.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) btnBack.getScene().getWindow();

            try {
                Object ctrl = loader.getController();
                if (ctrl instanceof ClienteController c) {
                    c.init(this.usuario);
                    c.setCoordinador(this.coordinador);
                }
            } catch (Exception ignore) {}

            Scene current = stage.getScene();
            double w = current != null ? current.getWidth() : 900;
            double h = current != null ? current.getHeight() : 600;
            stage.setScene(new Scene(root, w > 0 ? w : 900, h > 0 ? h : 600));
            stage.setTitle("Sigma Cine");
            stage.setMaximized(true);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void setBackResults(List<Pelicula> peliculas, String textoBuscado) {
        // reserved for future: maintain a reference to go back to results page
    }

    public void setPelicula(Pelicula p) {
        this.pelicula = p;
        if (p == null) return;

        String posterRef = safe(p.getPosterUrl());
        if (!posterRef.isEmpty()) {
            try {
                Image resolved = resolveImage(posterRef);
                if (imgPoster != null) imgPoster.setImage(resolved);
            } catch (Exception ignored) {
                if (imgPoster != null) imgPoster.setImage(null);
            }
        } else {
            if (imgPoster != null) imgPoster.setImage(null);
        }

        if (lblSinopsisTitulo != null) lblSinopsisTitulo.setText("SINOPSIS — " + safe(p.getTitulo(), "N/D"));
    if (lblTituloPelicula != null) lblTituloPelicula.setText(safe(p.getTitulo(), "N/D"));
        if (lblGenero != null) lblGenero.setText(safe(p.getGenero(), "N/D"));
        if (lblClasificacion != null) lblClasificacion.setText(safe(p.getClasificacion(), "N/D"));
        int dur = p.getDuracion();
        if (lblDuracion != null) lblDuracion.setText(dur > 0 ? dur + " min" : "N/D");
        if (lblDirector != null) lblDirector.setText(safe(p.getDirector(), "N/D"));
        if (lblReparto != null) lblReparto.setText(safe(p.getReparto(), ""));
        if (txtSinopsis != null) txtSinopsis.setText(safe(p.getSinopsis()));

        // Cargar funciones por ciudad/sede/sala
        try {
            if (panelFunciones != null) {
                panelFunciones.getChildren().clear();
                DatabaseConfig db = new DatabaseConfig();
                FuncionRepository repo = new FuncionRepositoryJdbc(db);
                List<FuncionDisponibleDTO> funciones = repo.listarPorPelicula(p.getId());
                String city = Session.getSelectedCity();
                if (city != null && !city.isBlank()) {
                    funciones = funciones.stream()
                            .filter(f -> city.equalsIgnoreCase(f.getCiudad()))
                            .toList();
                }
                renderFunciones(funciones);
            }
        } catch (Exception ignored) {}
    }

    public void setUsuario(UsuarioDTO u) { this.usuario = u; }
    public void setCoordinador(ControladorControlador c) { this.coordinador = c; }

    private static String safe(String s) {
        return (s == null || s.trim().equalsIgnoreCase("null")) ? "" : s.trim();
    }
    private static String safe(String s, String alt) {
        String t = safe(s);
        return t.isEmpty() ? alt : t;
    }

    private Image resolveImage(String ref) {
        if (ref == null || ref.isBlank()) return null;
        try {
            String lower = ref.toLowerCase();
            if (lower.startsWith("http://") || lower.startsWith("https://") || lower.startsWith("file:/")) {
                return new Image(ref, true);
            }
            java.net.URL res = getClass().getResource("/Images/" + ref);
            if (res != null) {
                return new Image(res.toExternalForm(), false);
            }
            File f = new File(ref);
            if (f.exists()) {
                return new Image(f.toURI().toString(), false);
            }
            res = getClass().getResource(ref.startsWith("/") ? ref : ("/" + ref));
            if (res != null) {
                return new Image(res.toExternalForm(), false);
            }
        } catch (Exception ignored) {}
        return null;
    }

    private void renderFunciones(List<FuncionDisponibleDTO> funciones) {
        if (funciones == null || funciones.isEmpty()) return;

        String currentCiudad = null;
        String currentSede = null;
        VBox sedeBox = null;
        VBox ciudadBox = null;

        for (FuncionDisponibleDTO f : funciones) {
            if (!f.getCiudad().equals(currentCiudad)) {
                currentCiudad = f.getCiudad();
                currentSede = null;
                ciudadBox = new VBox(6);
                Label lblCiudad = new Label(currentCiudad);
                lblCiudad.setStyle("-fx-text-fill:white;-fx-font-weight:bold;-fx-font-size:16;");
                ciudadBox.getChildren().add(lblCiudad);
                panelFunciones.getChildren().add(ciudadBox);
            }

            if (!f.getSede().equals(currentSede)) {
                currentSede = f.getSede();
                Label lblSede = new Label(currentSede);
                lblSede.setStyle("-fx-text-fill:#e5e7eb;-fx-font-weight:bold;-fx-font-size:14;");
                sedeBox = new VBox(4);
                sedeBox.getChildren().add(lblSede);
                if (ciudadBox != null) ciudadBox.getChildren().add(sedeBox);
            }

            // fila de horas por sala
            HBox fila = new HBox(6);
            fila.setStyle("-fx-background-color:transparent;");
            String pillText = String.format("%s — Sala %d %s", f.getHora().toString(), f.getNumeroSala(), f.getTipoSala());
            Button b = new Button(pillText);
            b.setStyle("-fx-background-color:transparent;-fx-border-color:#ffffff66;-fx-text-fill:white;-fx-background-radius:20;-fx-border-radius:20;-fx-padding:4 10 4 10;");
            b.setOnAction(e -> seleccionarFuncionPill(pillText));
            fila.getChildren().add(b);
            if (sedeBox != null) sedeBox.getChildren().add(fila);
        }
    }

    private void seleccionarFuncionPill(String texto) {
        if (lvFunciones != null) {
            if (!lvFunciones.getItems().contains(texto)) {
                lvFunciones.getItems().add(texto);
            }
            lvFunciones.getSelectionModel().select(texto);
        }
        if (lblHoraPill != null) lblHoraPill.setText(texto);
    }

    @FXML private void onComprarTickets(javafx.event.ActionEvent e) { onComprarTickets(); }

    @FXML
    private void onComprarTickets() {
        try {
            if (isEmbedded()) {
                String titulo = (lblTitulo != null && lblTitulo.getText() != null && !lblTitulo.getText().isBlank())
                        ? lblTitulo.getText() : "Película";
                String hora = (lvFunciones != null && lvFunciones.getSelectionModel() != null
                        && lvFunciones.getSelectionModel().getSelectedItem() != null)
                        ? lvFunciones.getSelectionModel().getSelectedItem()
                        : "1:10 pm";

                Set<String> ocupados   = Set.of("B3","B4","C7","E2","F8");
                Set<String> accesibles = Set.of("E3","E4","E5","E6");

                host.mostrarAsientos(titulo, hora, ocupados, accesibles);
                return;
            }

            String titulo = (lblTitulo != null && lblTitulo.getText() != null && !lblTitulo.getText().isBlank())
                    ? lblTitulo.getText() : "Película";
            String hora = (lvFunciones != null && lvFunciones.getSelectionModel() != null
                    && lvFunciones.getSelectionModel().getSelectedItem() != null)
                    ? lvFunciones.getSelectionModel().getSelectedItem()
                    : "1:10 pm";

            URL url = getClass().getResource("/sigmacine/ui/views/asientos.fxml");
            if (url == null) {
                var a = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
                a.setHeaderText("No se encontró asientos.fxml");
                a.setContentText("Ruta esperada: /sigmacine/ui/views/asientos.fxml");
                a.showAndWait();
                return;
            }

            FXMLLoader loader = new FXMLLoader(url);
            Parent root = loader.load();

            AsientosController ctrl = loader.getController();
            Set<String> ocupados   = Set.of("B3","B4","C7","E2","F8");
            Set<String> accesibles = Set.of("E3","E4","E5","E6");
            ctrl.setFuncion(titulo, hora, ocupados, accesibles);

            String posterResource = (pelicula != null && pelicula.getPosterUrl() != null && !pelicula.getPosterUrl().isBlank())
                    ? pelicula.getPosterUrl() : null;
            if (posterResource != null) {
                var is = getClass().getResourceAsStream(posterResource);
                if (is != null) ctrl.setPoster(new Image(is));
            }

            Stage stage = (Stage) btnComprar.getScene().getWindow();
            Scene current = stage.getScene();
            double w = current != null ? current.getWidth() : 1100;
            double h = current != null ? current.getHeight() : 620;

            stage.setScene(new Scene(root, w, h));
            stage.setMaximized(true);
            stage.setTitle("Selecciona tus asientos");
            stage.show();

        } catch (Exception ex) {
            ex.printStackTrace();
            var a = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
            a.setHeaderText("Error abriendo Asientos");
            a.setContentText(String.valueOf(ex));
            a.showAndWait();
        }
    }

    private boolean isEmbedded() {
        try {
            return host != null
                    && btnComprar != null
                    && host.isSameScene(btnComprar);
        } catch (Throwable t) {
            return false;
        }
    }
}
