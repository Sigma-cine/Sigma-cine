package sigmacine.ui.controller;

import javafx.scene.layout.Region;
import javafx.stage.Modality;
import javafx.scene.input.KeyCode;
import javafx.scene.effect.Effect;
import javafx.scene.effect.GaussianBlur; //Muestra el fondo desenfocado
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
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
// Dependencias necesarias para instanciación manual
import sigmacine.aplicacion.service.VerHistorialService;
import sigmacine.infraestructura.configDataBase.DatabaseConfig;
import sigmacine.infraestructura.persistencia.jdbc.PeliculaRepositoryJdbc;
import sigmacine.infraestructura.persistencia.jdbc.UsuarioRepositoryJdbc; 


public class ClienteController {

    // --- VISTA PRINCIPAL (cliente_home.fxml) ---
    @FXML private Button btnPromoVerMas;
    @FXML private Button btnCartelera;
    @FXML private Button btnConfiteria;
    @FXML private Button btnSigmaCard;
    @FXML private Button btnCart;
    @FXML private MenuItem miCerrarSesion;
    @FXML private MenuItem miHistorial;
    @FXML private StackPane promoPane;
    @FXML private ImageView imgPublicidad;
    @FXML private TextField txtBuscar;
    @FXML private javafx.scene.control.Button btnBuscar;
    @FXML private StackPane content;
    
    @FXML private ChoiceBox<String> cbCiudad;
    @FXML private Button btnSeleccionarCiudad;

    private UsuarioDTO usuario;
    private String ciudadSeleccionada;
    

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

        if (btnCartelera!= null) btnCartelera.setOnAction(e -> System.out.println("Ir a Cartelera (" + safeCiudad() + ")"));
        if (btnConfiteria != null) btnConfiteria.setOnAction(e -> System.out.println("Ir a Confitería (" + safeCiudad() + ")"));
        if (miCerrarSesion != null) miCerrarSesion.setOnAction(e -> onLogout());
        
        if (miHistorial != null) miHistorial.setOnAction(e -> onVerHistorial()); // Llama al método corregido.

        if (btnCart != null) btnCart.setOnAction(e -> openCartModal());

        if (txtBuscar != null) {
            txtBuscar.setOnKeyPressed(ev -> {
                if (ev.getCode() == KeyCode.ENTER) {
                    doSearch(txtBuscar.getText());
                }
            });
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
            stage.setScene(new Scene(root));
            stage.show();
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
            controller.setResultados(resultados, texto);

            javafx.stage.Stage stage = (javafx.stage.Stage) content.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Resultados de búsqueda");

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
            // Asume que VerHistorialService requiere un repositorio de Usuario para buscar el historial.
            var usuarioRepo = new UsuarioRepositoryJdbc(dbConfig);
            var historialService = new VerHistorialService(usuarioRepo);
            
            // Carga el FXML (ruta verificada y correcta)
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/sigmacine/ui/views/VerCompras.fxml"));
            
            VerHistorialController historialController = new VerHistorialController(historialService);
            historialController.setClienteController(this);

            if (this.usuario != null) {
                historialController.setUsuarioEmail(this.usuario.getEmail());
            }

            loader.setController(historialController);
            
            Parent historialView = loader.load();
            
            // 3. Muestra la vista
            content.getChildren().setAll(historialView);
            
        } catch (Exception ex) {
            System.err.println("Error cargando HistorialDeCompras.fxml: " + ex.getMessage());
            ex.printStackTrace();
            content.getChildren().setAll(new Label("Error cargando Historial: " + ex.getMessage()));
        }
    }
    
    public void mostrarCartelera() {
    System.out.println("Volviendo a Cartelera (Inicio).");
    try {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/sigmacine/ui/views/pagina_inicial.fxml"));
        Parent carteleraView = loader.load();

        ClienteController controller = loader.getController();
        controller.init(this.usuario, this.ciudadSeleccionada);
        content.getChildren().setAll(carteleraView);
    } catch (Exception e) {
        content.getChildren().setAll(new Label("Error: No se pudo cargar la vista de inicio."));
        e.printStackTrace();
        }
    }

private boolean cartOpen = false;

private void openCartModal() {
    if (cartOpen) return;
    Stage owner = (Stage) btnCart.getScene().getWindow();
    var ownerRoot = owner.getScene().getRoot();
    var prev = ownerRoot.getEffect();

    try {
        cartOpen = true;
        ownerRoot.setEffect(new javafx.scene.effect.GaussianBlur(18));

        FXMLLoader fx = new FXMLLoader(getClass().getResource("/sigmacine/ui/views/prueba.fxml"));
        Parent root = fx.load();

        if (root instanceof javafx.scene.layout.Region r) {
            if (r.getMinWidth() == Region.USE_COMPUTED_SIZE)  r.setMinWidth(600);
            if (r.getMinHeight() == Region.USE_COMPUTED_SIZE) r.setMinHeight(400);

        }

        root.setStyle(root.getStyle() + "; -fx-background-color: rgba(0,0,0,0.92);");


        Stage dialog = new Stage(javafx.stage.StageStyle.DECORATED);
        dialog.initModality(javafx.stage.Modality.WINDOW_MODAL);
        dialog.initOwner(owner);
        dialog.setTitle("Carrito");
        Scene sc = new Scene(root);
        dialog.setScene(sc);
        dialog.sizeToScene();
        dialog.centerOnScreen();

        sc.setOnKeyPressed(ev -> { if (ev.getCode() == javafx.scene.input.KeyCode.ESCAPE) dialog.close(); });

        dialog.setOnHidden(ev -> {
            ownerRoot.setEffect(prev);
            cartOpen = false;
        });

        dialog.show();
        dialog.requestFocus();

    } catch (Exception ex) {
        ownerRoot.setEffect(prev);
        cartOpen = false;
        ex.printStackTrace();
        throw new RuntimeException("Error mostrando el carrito (prueba.fxml)", ex);
    }
    }
}