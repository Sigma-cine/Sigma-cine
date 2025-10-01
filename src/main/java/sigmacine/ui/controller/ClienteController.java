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

    // Botones que existen en pagina_inicial.fxml
    @FXML private Button btnPromoVerMas;
    @FXML private Button btnCard1, btnCard2, btnCard3, btnCard4;

    // Si en el top tienes estos botones, déjalos:
    @FXML private Button btnCartelera;
    @FXML private Button btnConfiteria;
    @FXML private Button btnSigmaCard; // si no existe en FXML, quedará null (no pasa nada)
    @FXML private Button btnCart;      // idem
    @FXML private MenuItem miCerrarSesion;

    // Estado
    private UsuarioDTO usuario;

    // === Inicialización tras login (no carga FXML, no anida nada) ===
    public void init(UsuarioDTO usuario) {
        this.usuario = usuario;
        // Nada de goInicial() ni loadIntoContent(...)
    }

    @FXML
    private void initialize() {
        // Wiring simple (sin navegar a otra escena por ahora)
        if (btnCartelera  != null) btnCartelera.setOnAction(e -> System.out.println("Ir a Cartelera"));
        if (btnConfiteria != null) btnConfiteria.setOnAction(e -> System.out.println("Ir a Confitería"));
        if (miCerrarSesion != null) miCerrarSesion.setOnAction(e -> onLogout());
    }

    private void onLogout() {
        System.out.println("Cerrar sesión de: " + (usuario != null ? usuario.getEmail() : "desconocido"));
        // Aquí vuelves al login si quieres, pero sin anidar vistas.
    }

    // === Handlers que tu FXML invoca con onAction="#..." ===
    @FXML private void onPromoVerMas() { System.out.println("Promoción → Ver más"); }
    @FXML private void onCard1()       { System.out.println("Card 1 → Ver más"); }
    @FXML private void onCard2()       { System.out.println("Card 2 → Ver más"); }
    @FXML private void onCard3()       { System.out.println("Card 3 → Ver más"); }
    @FXML private void onCard4()       { System.out.println("Card 4 → Ver más"); }
}