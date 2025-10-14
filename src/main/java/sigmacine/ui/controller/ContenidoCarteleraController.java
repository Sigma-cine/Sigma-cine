package sigmacine.ui.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import sigmacine.aplicacion.data.UsuarioDTO;
import sigmacine.dominio.entity.Pelicula;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class ContenidoCarteleraController {

    @FXML private Button btnCarteleraTop;
    @FXML private Button btnBack;

    @FXML private TextArea txtSinopsis;
    @FXML private Label lblSinopsisTitulo;
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
    private List<Pelicula> backPeliculas;
    private String backTexto;

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
    }

    @FXML
    private void onCartelera() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/sigmacine/ui/views/contenidoCartelera.fxml"));
            Parent root = loader.load();
            ContenidoCarteleraController ctrl = loader.getController();
            ctrl.setCoordinador(this.coordinador);
            ctrl.setUsuario(this.usuario);

            Stage stage = (Stage) btnCarteleraTop.getScene().getWindow();
            Scene current = stage.getScene();
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
                if (imgPoster != null) imgPoster.setImage(resolved);
            } catch (Exception ignored) {
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
