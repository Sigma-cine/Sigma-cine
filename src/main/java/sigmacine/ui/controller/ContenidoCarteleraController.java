package sigmacine.ui.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.stage.Modality;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;

import sigmacine.dominio.entity.Pelicula;
import java.util.List;
import sigmacine.aplicacion.data.UsuarioDTO;
import sigmacine.ui.controller.ControladorControlador;
import java.io.File;

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

    // Detail view controls
    @FXML private ImageView imgPoster;
    @FXML private TextArea txtSinopsis;
    @FXML private Label lblSinopsisTitulo;
    @FXML private Label lblTituloPelicula;
    @FXML private VBox panelFunciones;
    @FXML private Button btnComprar;
    @FXML private Label lblGenero, lblClasificacion, lblDuracion, lblDirector, lblReparto;

    private Pelicula pelicula;
    private UsuarioDTO usuario;
    private ControladorControlador coordinador;
    private List<Pelicula> backPeliculas;
    private String backTexto;

    @FXML
    private void initialize() {
        if (btnComprar != null) {
            btnComprar.setOnAction(e -> {
                
            });
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
            sigmacine.infraestructura.configDataBase.DatabaseConfig dbConfig = new sigmacine.infraestructura.configDataBase.DatabaseConfig();
            sigmacine.infraestructura.persistencia.jdbc.UsuarioRepositoryJdbc usuarioRepo = new sigmacine.infraestructura.persistencia.jdbc.UsuarioRepositoryJdbc(dbConfig);
            sigmacine.aplicacion.service.VerHistorialService historialService = new sigmacine.aplicacion.service.VerHistorialService(usuarioRepo);

            // Load full-screen history view (not a modal)
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/sigmacine/ui/views/verCompras.fxml"));
            VerHistorialController historialController = new VerHistorialController(historialService);
            // Inject email if available
            try {
                if (this.usuario != null && this.usuario.getEmail() != null) {
                    historialController.setUsuarioEmail(this.usuario.getEmail());
                } else {
                    // fallback to session current user if available
                    var cur = sigmacine.aplicacion.session.Session.getCurrent();
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
        System.out.println("[DEBUG] onCartelera invoked");
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
            javafx.scene.Scene current = stage.getScene();
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
        System.out.println("[DEBUG] onVolver invoked");
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/sigmacine/ui/views/pagina_inicial.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) btnBack.getScene().getWindow();

            // If we have a controller reference, initialize it with the current usuario and coordinador
            try {
                Object ctrl = loader.getController();
                if (ctrl instanceof ClienteController) {
                    ClienteController c = (ClienteController) ctrl;
                    c.init(this.usuario);
                    c.setCoordinador(this.coordinador);
                }
            } catch (Exception ignore) {}

            javafx.scene.Scene current = stage.getScene();
            double w = current != null ? current.getWidth() : 900;
            double h = current != null ? current.getHeight() : 600;
            stage.setScene(new Scene(root, w > 0 ? w : 900, h > 0 ? h : 600));
            stage.setTitle("Sigma Cine");
            stage.setMaximized(true);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    // Called by ResultadosBusquedaController when opening the detail view
    public void setBackResults(List<Pelicula> peliculas, String textoBuscado) {
        this.backPeliculas = peliculas;
        this.backTexto = textoBuscado;
    }

    public void setPelicula(Pelicula p) {
        this.pelicula = p;
        if (p == null) return;

        String posterRef = safe(p.getPosterUrl());
        if (!posterRef.isEmpty()) {
            try {
                Image resolved = resolveImage(posterRef);
                if (imgPoster != null) {
                    imgPoster.setImage(resolved);
                }
            } catch (Exception ignored) {
                // Leave image null if resolution fails
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

    /**
     * Resolve an image reference to a JavaFX Image using the following order:
     * 1. If ref is a http(s) or file: URL, use it directly (background loading for remote urls).
     * 2. If there's a classpath resource under /Images/<ref>, use it.
     * 3. If ref points to an existing local file, use its URI.
     * 4. If ref is an absolute/leading-slash classpath resource, try that too.
     * Returns null when nothing could be resolved.
     */
    private Image resolveImage(String ref) {
        if (ref == null || ref.isBlank()) return null;
        try {
            String lower = ref.toLowerCase();
            if (lower.startsWith("http://") || lower.startsWith("https://") || lower.startsWith("file:/")) {
                return new Image(ref, true);
            }
            // try classpath under /Images/
            java.net.URL res = getClass().getResource("/Images/" + ref);
            if (res != null) {
                return new Image(res.toExternalForm(), false);
            }
            // try local file
            File f = new File(ref);
            if (f.exists()) {
                return new Image(f.toURI().toString(), false);
            }
            // try as absolute/leading slash resource
            res = getClass().getResource(ref.startsWith("/") ? ref : ("/" + ref));
            if (res != null) {
                return new Image(res.toExternalForm(), false);
            }
        } catch (Exception e) {
            // ignore and return null
        }
        return null;
    }
}