package sigmacine.ui.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.MenuItem;
import javafx.stage.Stage;
import sigmacine.aplicacion.data.UsuarioDTO;


public class ClienteController {

    // --- VISTA PRINCIPAL (pagina_inicial.fxml) ---
    @FXML private Button btnPromoVerMas;
    @FXML private Button btnCard1, btnCard2, btnCard3, btnCard4;
    @FXML private Button btnCartelera;
    @FXML private Button btnConfiteria;
    @FXML private Button btnSigmaCard; // puede ser null si no existe en ese FXML
    @FXML private Button btnCart;      // idem
    @FXML private MenuItem miCerrarSesion;

    // --- VISTA CIUDAD (ciudad.fxml) ---
    @FXML private ChoiceBox<String> cbCiudad;
    @FXML private Button btnSeleccionarCiudad;

    // --- Estado ---
    private UsuarioDTO usuario;
    private String ciudadSeleccionada;

    // === Inicialización genérica (compat) ===
    public void init(UsuarioDTO usuario) {
        this.usuario = usuario;
    }

    /** Llamado cuando ya estás en la principal (pagina_inicial.fxml) */
    public void init(UsuarioDTO usuario, String ciudad) {
        this.usuario = usuario;
        this.ciudadSeleccionada = ciudad;
        // Aquí puedes usar ciudadSeleccionada para filtrar/mostrar contenido
    }

    /** Llamado cuando estás en la pantalla de ciudad (ciudad.fxml) */
    public void initCiudad(UsuarioDTO usuario) {
        this.usuario = usuario;
        if (cbCiudad != null) {
            cbCiudad.getItems().setAll("Bogotá", "Medellín", "Cali", "Barranquilla");
            cbCiudad.getSelectionModel().selectFirst();
        }
    }

    @FXML
    private void initialize() {
        // --- Wiring para pantalla CIUDAD ---
        if (btnSeleccionarCiudad != null) {
            btnSeleccionarCiudad.setOnAction(e -> onSeleccionarCiudad());
        }

        // --- Wiring para pantalla PRINCIPAL ---
        if (btnCartelera  != null) btnCartelera.setOnAction(e -> System.out.println("Ir a Cartelera (" + safeCiudad() + ")"));
        if (btnConfiteria != null) btnConfiteria.setOnAction(e -> System.out.println("Ir a Confitería (" + safeCiudad() + ")"));
        if (miCerrarSesion != null) miCerrarSesion.setOnAction(e -> onLogout());
    }

    private String safeCiudad() {
        return ciudadSeleccionada != null ? ciudadSeleccionada : "sin ciudad";
    }

    private void onLogout() {
        System.out.println("Cerrar sesión de: " + (usuario != null ? usuario.getEmail() : "desconocido"));
        
    }
    @FXML private void onPromoVerMas() { System.out.println("Promoción → Ver más (" + safeCiudad() + ")"); }
    @FXML private void onCard1()       { System.out.println("Card 1 → Ver más (" + safeCiudad() + ")"); }
    @FXML private void onCard2()       { System.out.println("Card 2 → Ver más (" + safeCiudad() + ")"); }
    @FXML private void onCard3()       { System.out.println("Card 3 → Ver más (" + safeCiudad() + ")"); }
    @FXML private void onCard4()       { System.out.println("Card 4 → Ver más (" + safeCiudad() + ")"); }


    private void onSeleccionarCiudad() {
        String ciudad = (cbCiudad != null) ? cbCiudad.getValue() : null;
        if (ciudad == null || ciudad.isBlank()) return;

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/sigmacine/ui/views/pagina_inicial.fxml"));
            Parent root = loader.load();

            // Reutilizamos el mismo controller (ClienteController) que ya está en pagina_inicial.fxml
            ClienteController controller = loader.getController();
            controller.init(this.usuario, ciudad);

            // Cambiar la escena en el Stage actual
            Stage stage = (Stage) btnSeleccionarCiudad.getScene().getWindow();
            stage.setTitle("Sigma Cine - Cliente (" + ciudad + ")");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception ex) {
            throw new RuntimeException("Error cargando pagina_inicial.fxml", ex);
        }
    }
}
