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

    // Detail view controls
    @FXML private ImageView imgPoster;
    @FXML private TextArea txtSinopsis;
    @FXML private Label lblSinopsisTitulo;
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
    }

    @FXML
    private void onCartelera() {
        System.out.println("[DEBUG] onCartelera invoked");
        try {
            // If we have a coordinator, use it to show cartelera and preserve session
            if (coordinador != null) {
                // mostrarHome without changing user — mostrarCartelera isn't available on coordinador,
                // so reload contenidoCartelera but pass the current usuario/coordinador
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/sigmacine/ui/views/contenidoCartelera.fxml"));
                Parent root = loader.load();
                ContenidoCarteleraController ctrl = loader.getController();
                ctrl.setCoordinador(this.coordinador);
                ctrl.setUsuario(this.usuario);

                Stage stage = (Stage) btnCarteleraTop.getScene().getWindow();
                javafx.scene.Scene current = stage.getScene();
                double w = current != null ? current.getWidth() : 900;
                double h = current != null ? current.getHeight() : 600;
                stage.setScene(new Scene(root, w > 0 ? w : 900, h > 0 ? h : 600));
                stage.setMaximized(true);
                return;
            }
            // fallback: reload without session
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/sigmacine/ui/views/contenidoCartelera.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) btnCarteleraTop.getScene().getWindow();
            javafx.scene.Scene current = stage.getScene();
            double w = current != null ? current.getWidth() : 900;
            double h = current != null ? current.getHeight() : 600;
            stage.setScene(new Scene(root, w > 0 ? w : 900, h > 0 ? h : 600));
            stage.setMaximized(true);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
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