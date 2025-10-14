package sigmacine.ui.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.StackPane;
import sigmacine.aplicacion.data.CompraProductoDTO;
import sigmacine.aplicacion.service.CarritoService;

public class VerCarritoController {

    @FXML private StackPane carritoRoot;
    @FXML private ListView<CompraProductoDTO> listaItems;
    @FXML private Label lblTotal;

    private final CarritoService carrito = CarritoService.getInstance();

    @FXML
    private void initialize() {
        if (listaItems != null) listaItems.setItems(carrito.getItems());
        updateTotal();
        carrito.addListener(c -> updateTotal());
    }

    private void updateTotal() {
        if (lblTotal != null) lblTotal.setText(carrito.getTotal().toString());
    }
}
