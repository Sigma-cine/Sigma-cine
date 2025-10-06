package sigmacine.ui.controller;

import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Region;

import sigmacine.aplicacion.service.VerHistorialService;
import sigmacine.dominio.entity.Compra;
import sigmacine.dominio.entity.Boleto;
import sigmacine.dominio.entity.Usuario;

public class VerHistorialController {
    
    @FXML
    private VBox comprasContainer;
    
    @FXML
    private Button btnVolver;
    
    private ClienteController clienteController;
    
    private final VerHistorialService historialService;
    private String usuarioEmail;

    public VerHistorialController(VerHistorialService historialService) {
        this.historialService = historialService;
    }
    
    public void setClienteController(ClienteController controller) {
        this.clienteController = controller;
    }
    
    public void setUsuarioEmail(String email) {
        this.usuarioEmail = email;
    }

    @FXML
    public void initialize() {
        // Asumiendo que btnVolver existe en el FXML
        if (btnVolver != null) {
            btnVolver.setOnAction(e -> onVolverAInicio());
        }
        
        // Carga de datos después de asegurar que las dependencias estén inyectadas (setUsuarioEmail)
        if (comprasContainer != null) {
            comprasContainer.getChildren().clear(); 
            cargarHistorialDeCompras();
        }
    }
    
    private void cargarHistorialDeCompras() {
        if (usuarioEmail == null || usuarioEmail.isEmpty()) {
            comprasContainer.getChildren().add(new Label("Error: Email de usuario no disponible para la búsqueda."));
            return;
        }

        try {
            // El servicio debe devolver List<Compra>
            List<Compra> historial = historialService.verHistorial(usuarioEmail);

            if (historial.isEmpty()) {
                comprasContainer.getChildren().add(new Label("No has realizado ninguna compra aún."));
                return;
            }

            for (Compra compra : historial) {
                HBox tarjetaCompra = crearTarjetaCompra(compra);
                comprasContainer.getChildren().add(tarjetaCompra);
            }

        } catch (IllegalArgumentException e) {
            comprasContainer.getChildren().add(new Label("Error: " + e.getMessage()));
        } catch (Exception e) {
            comprasContainer.getChildren().add(new Label("Ocurrió un error al cargar el historial: " + e.getMessage()));
            e.printStackTrace();
        }
    }

    private HBox crearTarjetaCompra(Compra compra) {
        
        String titulo = "Compra de Confitería y/o Sin Boletos";
        String ubicacion = "N/A";
        String fechaHora = "N/A";
        
        if (!compra.getBoletos().isEmpty()) {
            Boleto primerBoleto = compra.getBoletos().get(0);
            titulo = primerBoleto.getPelicula();
            ubicacion = primerBoleto.getSala();
            fechaHora = primerBoleto.getHorario();
        }
        
        HBox tarjeta = new HBox(20);
        tarjeta.setPadding(new javafx.geometry.Insets(10, 15, 10, 15));
        tarjeta.setPrefHeight(150);
        tarjeta.getStyleClass().add("tarjeta-compra");
        
        VBox detalles = new VBox(5);
        detalles.getChildren().add(new Label(titulo));
        detalles.getChildren().add(new Label("Ubicación: " + ubicacion));
        detalles.getChildren().add(new Label("Fecha/Hora: " + fechaHora));
        detalles.getChildren().add(new Label("ID: " + compra.getId()));
        
        Label total = new Label("Total: " + compra.getTotal());
        total.getStyleClass().add("venue-box");
        detalles.getChildren().add(total);
        
        HBox.setHgrow(detalles, javafx.scene.layout.Priority.ALWAYS);

        Button verDetalleBtn = new Button("Ver Detalle");
        verDetalleBtn.getStyleClass().add("buy-btn");
        
        Region posterPlaceholder = new Region();
        posterPlaceholder.setPrefSize(100, 130);
        posterPlaceholder.setStyle("-fx-background-color: #333333;");
        
        tarjeta.getChildren().addAll(posterPlaceholder, detalles, verDetalleBtn);
        return tarjeta;
    }
    
    @FXML
    private void onVolverAInicio() {
        if (clienteController != null) {
            clienteController.mostrarCartelera(); 
        } else {
            System.err.println("Error: ClienteController no inyectado. No se puede volver.");
        }
    }
}