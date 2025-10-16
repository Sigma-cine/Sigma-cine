package sigmacine.ui.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.RadioButton;
import javafx.stage.Stage;
import sigmacine.aplicacion.service.CarritoService;
import sigmacine.aplicacion.data.CompraProductoDTO;

import java.math.BigDecimal;

public class PagoController {

    @FXML private ListView<CompraProductoDTO> listaResumen;
    @FXML private Label lblTotal;
    @FXML private RadioButton rbCredito;
    @FXML private RadioButton rbDebito;
    @FXML private Button btnPagar;

    private final CarritoService carrito = CarritoService.getInstance();

    @FXML
    private void initialize() {
        if (listaResumen != null) listaResumen.setItems(carrito.getItems());
        actualizarTotal();
        carrito.addListener(c -> actualizarTotal());
        if (btnPagar != null) btnPagar.setOnAction(e -> realizarPago());
    }

    private void actualizarTotal() {
        BigDecimal t = carrito.getTotal();
        if (lblTotal != null) lblTotal.setText("$" + t.toPlainString());
    }

    private void realizarPago() {
        // Aquí podríamos persistir la compra; por ahora solo mostramos confirmación
        javafx.scene.control.Alert ok = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
        ok.setTitle("Pago exitoso");
        ok.setHeaderText(null);
        ok.setContentText("Compra realizada con éxito. ¡Disfruta tu película!");
        ok.showAndWait();
        // Cerrar ventana de pago
        Stage st = (Stage) btnPagar.getScene().getWindow();
        st.close();
    }
}
