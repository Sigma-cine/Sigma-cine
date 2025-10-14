package sigmacine.ui.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.StackPane;
import javafx.scene.control.Button;
import java.math.BigDecimal;
import sigmacine.aplicacion.data.CompraProductoDTO;
import sigmacine.aplicacion.service.CarritoService;

public class VerCarritoController {

    @FXML private StackPane carritoRoot;
    @FXML private ListView<CompraProductoDTO> listaItems;
    @FXML private Label lblTotal;       // total de tiquetes (subtotal)
    @FXML private Label lblTotalGlobal; // TOTAL grande (si lo agregamos en FXML)
    @FXML private Button btnProcederPago;

    private final CarritoService carrito = CarritoService.getInstance();

    @FXML
    private void initialize() {
        if (listaItems != null) listaItems.setItems(carrito.getItems());
        updateTotal();
        carrito.addListener(c -> updateTotal());

        // Handler principal viene del FXML onAction="#onProcederPago"; no reasignamos aquí.
    }

    private void updateTotal() {
        try {
            BigDecimal total = carrito.getTotal();
            if (lblTotal != null) lblTotal.setText("$" + total.toPlainString());
            if (lblTotalGlobal != null) lblTotalGlobal.setText("$" + total.toPlainString());
        } catch (Exception ignore) {}
    }

    // Permite que otros controladores fuercen el refresco (compatibilidad)
    public void refresh() {
        if (listaItems != null) listaItems.refresh();
        updateTotal();
    }

    private void abrirPantallaPago() {
        try {
            if (carrito.getItems().isEmpty()) {
                javafx.scene.control.Alert a = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION, "El carrito está vacío.");
                a.setHeaderText(null);
                a.setTitle("Carrito vacío");
                a.showAndWait();
                return;
            }
            var loader = new javafx.fxml.FXMLLoader(getClass().getResource("/sigmacine/ui/views/pago.fxml"));
            javafx.scene.Parent root = loader.load();
            var stage = new javafx.stage.Stage();
            stage.setTitle("Pago");
            stage.setScene(new javafx.scene.Scene(root, 480, 520));
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            stage.showAndWait();
        } catch (Exception ex) {
            ex.printStackTrace();
            javafx.scene.control.Alert err = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR, "No se pudo abrir la pantalla de pago: " + ex.getMessage());
            err.setHeaderText("Error");
            err.showAndWait();
        }
    }

    // Handler invocado desde el FXML (onAction="#onProcederPago")
    @FXML
    private void onProcederPago() {
        abrirPantallaPago();
    }
}
