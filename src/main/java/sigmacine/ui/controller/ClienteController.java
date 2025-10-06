package sigmacine.ui.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import sigmacine.aplicacion.data.UsuarioDTO;
import javafx.scene.image.ImageView;      // ⬅️ IMPORT CLAVE
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import sigmacine.infraestructura.configDataBase.DatabaseConfig;
import sigmacine.infraestructura.persistencia.jdbc.PeliculaRepositoryJdbc;


public class ClienteController {

    // --- VISTA PRINCIPAL (pagina_inicial.fxml) ---
    @FXML private Button btnPromoVerMas;
    @FXML private Button btnCard1, btnCard2, btnCard3, btnCard4;
    @FXML private Button btnCartelera;
    @FXML private Button btnConfiteria;
    @FXML private Button btnSigmaCard; // puede ser null si no existe en ese FXML
    @FXML private Button btnCart;      // idem
    @FXML private MenuItem miCerrarSesion;
    @FXML private StackPane promoPane;
    @FXML private ImageView imgPublicidad;
    @FXML private TextField txtBuscar;
    @FXML private javafx.scene.control.Button btnBuscar;
    @FXML private StackPane content;

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
        // Banner: ocupa todo el contenedor
        if (promoPane != null && imgPublicidad != null) {
        imgPublicidad.fitWidthProperty().bind(promoPane.widthProperty());
        imgPublicidad.setFitHeight(110);   // fijo
        imgPublicidad.setPreserveRatio(true); 
         }
        // --- Wiring para pantalla CIUDAD ---
        if (btnSeleccionarCiudad != null) {
            btnSeleccionarCiudad.setOnAction(e -> onSeleccionarCiudad());
        }

        // --- Wiring para pantalla PRINCIPAL ---
        if (btnCartelera  != null) btnCartelera.setOnAction(e -> System.out.println("Ir a Cartelera (" + safeCiudad() + ")"));
        if (btnConfiteria != null) btnConfiteria.setOnAction(e -> System.out.println("Ir a Confitería (" + safeCiudad() + ")"));
        if (miCerrarSesion != null) miCerrarSesion.setOnAction(e -> onLogout());

        // Wiring del buscador: al presionar Enter abre la vista de búsqueda y muestra resultados
        if (txtBuscar != null) {
            txtBuscar.setOnKeyPressed(ev -> {
                if (ev.getCode() == KeyCode.ENTER) {
                    doSearch(txtBuscar.getText());
                }
            });
            // botón de búsqueda explícito
            if (btnBuscar != null) btnBuscar.setOnAction(e -> doSearch(txtBuscar.getText()));
        }
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

    @FXML
    private void onBuscarTop() {
        // Método invocado desde cliente_home.fxml cuando se presiona el botón Buscar
        System.out.println("onBuscarTop clicked, txtBuscar='" + (txtBuscar != null ? txtBuscar.getText() : "<null>") + "'");
        doSearch(txtBuscar != null ? txtBuscar.getText() : "");
    }

    /** Carga la vista buscarPeliculas.fxml dentro del content y ejecuta la búsqueda usando el repo JDBC */
    private void doSearch(String texto) {
        if (texto == null) texto = "";
        System.out.println("doSearch invoked with: '" + texto + "'");
        try {
            // Buscar películas
            DatabaseConfig db = new DatabaseConfig();
            var repo = new PeliculaRepositoryJdbc(db);
            var resultados = repo.buscarPorTitulo(texto);

            // Cargar la pantalla de resultados en el mismo Stage principal
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/sigmacine/ui/views/resultados_busqueda.fxml"));
            Parent root = loader.load();
            ResultadosBusquedaController controller = loader.getController();
            controller.setResultados(resultados, texto);

            // Obtener el Stage principal desde cualquier nodo de la UI
            javafx.stage.Stage stage = (javafx.stage.Stage) content.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Resultados de búsqueda");

        } catch (Exception ex) {
            System.err.println("Error cargando resultados_busqueda.fxml: " + ex.getMessage());
            ex.printStackTrace();
            throw new RuntimeException("Error cargando resultados_busqueda.fxml", ex);
        }
    }
}
