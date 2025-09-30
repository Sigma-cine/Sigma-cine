/*package sigmacine.ui.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import sigmacine.aplicacion.data.UsuarioDTO;

public class ClienteController {

    @FXML private Label welcomeLabel;

    private UsuarioDTO usuario;
    private ControladorControlador coordinador;

    public void init(UsuarioDTO usuario, ControladorControlador coordinador) {
        this.usuario = usuario;
        this.coordinador = coordinador;
        if (welcomeLabel != null) welcomeLabel.setText("Bienvenido al Cine Sigma");
    }

    @FXML
    private void onLogout() {
        if (coordinador != null) coordinador.mostrarLogin();
    }

    // Aquí irán acciones propias del cliente (cartelera, compras, etc.)
}
*/
package sigmacine.ui.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.StackPane;
import sigmacine.aplicacion.data.UsuarioDTO;

import java.net.URL;
import java.util.Objects;

public class ClienteController {

    @FXML private StackPane content;

    @FXML private Button btnCartelera;
    @FXML private Button btnConfiteria;
    @FXML private Button btnSigmaCard;
    @FXML private Button btnCart;
    @FXML private MenuItem miCerrarSesion;

    private UsuarioDTO usuario;
    private ControladorControlador coordinador;

    // Rutas FXML (en classpath)
    private static final String FXML_CARTELERA  = "/sigmacine/ui/views/contenidoCartelera.fxml";
    private static final String FXML_CONFITERIA = "/sigmacine/ui/views/confiteria.fxml";
    //private static final String FXML_SIGMACARD  = "/sigmacine/ui/views/pagina_inicial.fxml"; // temporal

    public void init(UsuarioDTO usuario, ControladorControlador coordinador) {
        this.usuario = usuario;
        this.coordinador = coordinador;
        goCartelera();
    }

    @FXML
    private void initialize() {
        if (btnCartelera  != null) btnCartelera.setOnAction(e -> goCartelera());
        if (btnConfiteria != null) btnConfiteria.setOnAction(e -> goConfiteria());
     //   if (btnSigmaCard  != null) btnSigmaCard.setOnAction(e -> goSigmaCard());
        if (miCerrarSesion != null) miCerrarSesion.setOnAction(e -> onLogout());

        // Por si no llaman init()
        if (content != null && content.getChildren().isEmpty()) {
            goCartelera();
        }
    }

    private void onLogout() {
        if (coordinador != null) coordinador.mostrarLogin();
    }

    // Navegación interna
    private void goCartelera()  { loadIntoContent(FXML_CARTELERA); }
    private void goConfiteria() { loadIntoContent(FXML_CONFITERIA); }
    //private void goSigmaCard()  { loadIntoContent(FXML_SIGMACARD); }

    private void loadIntoContent(String fxmlPath) {
        try {
            URL url = Objects.requireNonNull(
                ClienteController.class.getResource(fxmlPath),
                "No se encontró el recurso FXML: " + fxmlPath
            );
            FXMLLoader loader = new FXMLLoader(url);
            Node node = loader.load();
            Object ctrl = loader.getController();

            // Si tus sub-controladores existen, inyéctales el coordinador aquí
            if (ctrl instanceof CarteleraController cc) {
                cc.setCoordinador(coordinador);
            } else if (ctrl instanceof ConfiteriaController cf) {
                cf.setCoordinador(coordinador);
            }
            content.getChildren().setAll(node);
        } catch (Exception e) {
            throw new RuntimeException("Error cargando subvista: " + fxmlPath, e);
        }
    }
}
