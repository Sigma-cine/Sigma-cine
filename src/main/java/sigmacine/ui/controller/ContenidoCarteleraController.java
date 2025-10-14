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

    // Opcionales (si existen en el FXML)
    @FXML private StackPane trailerContainer;
    @FXML private ScrollPane spCenter;
    @FXML private VBox detalleRoot;

    private Pelicula pelicula;
    private UsuarioDTO usuario;
    private ControladorControlador coordinador;
    private List<Pelicula> backPeliculas;
    private String backTexto;

    // Host cuando esta vista se incrusta dentro de pagina_inicial -> content
    private ClienteController host;
    public void setHost(ClienteController host) { this.host = host; }

    @FXML
    private void initialize() {
        System.out.println("[ContenidoCartelera] initialize @" + System.identityHashCode(this));

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

            btnComprar.setOnAction(e -> {
                System.out.println("[ContenidoCartelera] onAction btnComprar @" + System.identityHashCode(this));
                onComprarTickets();
            });
        }
    }

    @FXML
    private void onCartelera() {
        System.out.println("[ContenidoCartelera] onCartelera()");
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/sigmacine/ui/views/contenidoCartelera.fxml"));
            Parent root = loader.load();
            ContenidoCarteleraController ctrl = loader.getController();
            ctrl.setCoordinador(this.coordinador);
            ctrl.setUsuario(this.usuario);
            // NOTA: aquí NO seteamos host a menos que realmente se vaya a embeber

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
        System.out.println("[ContenidoCartelera] onVolver()");
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/sigmacine/ui/views/pagina_inicial.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) btnBack.getScene().getWindow();

            try {
                Object ctrl = loader.getController();
                if (ctrl instanceof ClienteController) {
                    ClienteController c = (ClienteController) ctrl;
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

        String url = safe(p.getPosterUrl());
        if (!url.isEmpty()) {
            try { if (imgPoster != null) imgPoster.setImage(new Image(url, true)); } catch (Exception ignored) {}
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

    // onAction compatible con SceneBuilder
    @FXML private void onComprarTickets(javafx.event.ActionEvent e) { onComprarTickets(); }

    @FXML
    private void onComprarTickets() {
        try {
            // ¿Está REALMENTE embebido en el mismo Stage/Scene que el host?
            if (isEmbedded()) {
                String titulo = (lblTitulo != null && lblTitulo.getText() != null && !lblTitulo.getText().isBlank())
                        ? lblTitulo.getText() : "Película";
                String hora = (lvFunciones != null && lvFunciones.getSelectionModel() != null
                        && lvFunciones.getSelectionModel().getSelectedItem() != null)
                        ? lvFunciones.getSelectionModel().getSelectedItem()
                        : "1:10 pm";

                Set<String> ocupados   = Set.of("B3","B4","C7","E2","F8");
                Set<String> accesibles = Set.of("E3","E4","E5","E6");

                System.out.println("[ContenidoCartelera] host.mostrarAsientos (embebido)");
                host.mostrarAsientos(titulo, hora, ocupados, accesibles);
                return;
            }

            // No embebido → escena completa
            String titulo = (lblTitulo != null && lblTitulo.getText() != null && !lblTitulo.getText().isBlank())
                    ? lblTitulo.getText() : "Película";
            String hora = (lvFunciones != null && lvFunciones.getSelectionModel() != null
                    && lvFunciones.getSelectionModel().getSelectedItem() != null)
                    ? lvFunciones.getSelectionModel().getSelectedItem()
                    : "1:10 pm";

            URL url = getClass().getResource("/sigmacine/ui/views/asientos.fxml");
            System.out.println("[ContenidoCartelera] Cargando FXML asientos: " + url);
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

    /** Verdadero solo si:
     *  - tenemos host
     *  - y el botón actual vive en la MISMA Scene que el host (es decir, realmente embebido). */
    private boolean isEmbedded() {
        try {
            return host != null
                    && btnComprar != null
                    && host.isSameScene(btnComprar);   // usa el helper público del ClienteController
        } catch (Throwable t) {
            return false;
        }
    }
}