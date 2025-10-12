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
        // Si la vista ya fue inicializada y el contenedor existe, recargar historial inmediatamente.
        if (this.comprasContainer != null) {
            this.comprasContainer.getChildren().clear();
            cargarHistorialDeCompras();
        }
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
            // Limpiar nodos promocionales que puedan haber quedado en la escena
            javafx.application.Platform.runLater(() -> limpiarNodosPromocionales());
        }
    }

    // Elimina botones "Ver más" y labels genéricos que puedan provenir de la vista de inicio
    private void limpiarNodosPromocionales() {
        try {
            if (comprasContainer == null) return;
            var scene = comprasContainer.getScene();
            if (scene == null) return;
            var root = scene.getRoot();
            eliminarRecursivo(root);
        } catch (Exception e) {
            // no bloquear la carga si falla la limpieza
            System.err.println("Advertencia: no se pudo limpiar nodos promocionales: " + e.getMessage());
        }
    }

    private void eliminarRecursivo(javafx.scene.Parent parent) {
        if (parent == null) return;
        if (parent instanceof javafx.scene.layout.Pane) {
            javafx.scene.layout.Pane pane = (javafx.scene.layout.Pane) parent;
            // copiamos la lista para evitar ConcurrentModification
            java.util.List<javafx.scene.Node> copia = new java.util.ArrayList<>(pane.getChildren());
            for (javafx.scene.Node n : copia) {
                boolean removed = false;
                if (n instanceof javafx.scene.control.Button) {
                    javafx.scene.control.Button b = (javafx.scene.control.Button) n;
                    String t = b.getText();
                    if (t != null && t.trim().equalsIgnoreCase("Ver más")) {
                        pane.getChildren().remove(n);
                        removed = true;
                    }
                }
                if (!removed && n instanceof javafx.scene.control.Label) {
                    javafx.scene.control.Label lab = (javafx.scene.control.Label) n;
                    String lt = lab.getText();
                    if (lt != null && lt.trim().equalsIgnoreCase("Label")) {
                        pane.getChildren().remove(n);
                        removed = true;
                    }
                }
                if (!removed && n instanceof javafx.scene.Parent) {
                    eliminarRecursivo((javafx.scene.Parent) n);
                }
            }
        } else {
            java.util.List<javafx.scene.Node> children = parent.getChildrenUnmodifiable();
            for (javafx.scene.Node n : children) {
                if (n instanceof javafx.scene.Parent) eliminarRecursivo((javafx.scene.Parent) n);
            }
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
        
        VBox detalles = new VBox(6);
        // Título de la compra / película
        Label tituloLbl = new Label(titulo);
        tituloLbl.getStyleClass().add("section-subtitle");
        detalles.getChildren().add(tituloLbl);

        // Ubicación (sala) si está
        Label ubicacionLbl = new Label("Ubicación: " + ubicacion);
        ubicacionLbl.getStyleClass().add("small-muted");
        detalles.getChildren().add(ubicacionLbl);

        // Mostrar fecha si está disponible (si no, usar horario del boleto)
        String fechaText = (compra.getFecha() != null) ? compra.getFecha().toString() : fechaHora;
        Label fechaLbl = new Label("Fecha/Hora: " + fechaText);
        fechaLbl.getStyleClass().add("small-muted");
        detalles.getChildren().add(fechaLbl);

        // ID de la compra
        Label idLbl = new Label("ID: " + compra.getId());
        idLbl.getStyleClass().add("small-muted");
        detalles.getChildren().add(idLbl);

        // Si hay boletos, mostrar listado compacto de boletos
        if (!compra.getBoletos().isEmpty()) {
            VBox listaBoletos = new VBox(3);
            listaBoletos.setStyle("-fx-padding: 6 0 0 0;");
            for (Boleto bt : compra.getBoletos()) {
                String asiento = bt.getPelicula() != null ? bt.getPelicula() : "[película]";
                String linea = String.format("%s — Sala %s — Asiento %s — %.0f", asiento, bt.getSala(), bt.getHorario(), (double) bt.getPrecio());
                Label bLbl = new Label(linea);
                bLbl.getStyleClass().add("small-muted");
                listaBoletos.getChildren().add(bLbl);
            }
            detalles.getChildren().add(listaBoletos);
        }

        // Mostrar total decimal con dos decimales
        String totalStr = String.format("%.2f", compra.getTotalDecimal());
        Label total = new Label("Total: " + totalStr);
        total.getStyleClass().add("venue-box");
        detalles.getChildren().add(total);

        HBox.setHgrow(detalles, javafx.scene.layout.Priority.ALWAYS);

        Region posterPlaceholder = new Region();
        posterPlaceholder.setPrefSize(100, 130);
        posterPlaceholder.setStyle("-fx-background-color: #111111; -fx-border-radius: 4; -fx-background-radius: 4;");

        // Añadir solo imagen y detalles; no botones extra
        tarjeta.getChildren().addAll(posterPlaceholder, detalles);
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