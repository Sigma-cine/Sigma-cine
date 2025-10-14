package sigmacine.ui.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import sigmacine.aplicacion.service.CarritoService;
import sigmacine.dominio.entity.Boleto;
import sigmacine.dominio.entity.Producto;

/** Controlador simple para mostrar el contenido del carrito (verCarrito.fxml). */
public class VerCarritoController {

    @FXML private ListView<String> listaItems;
    @FXML private Label lblTotal;

    private final CarritoService carrito = CarritoService.getInstance();

    @FXML
    private void initialize() {
        refresh();
    }

    public void refresh() {
        if (listaItems == null) return;
        listaItems.getItems().clear();
        long totalCentavos = 0;
        for (Boleto b : carrito.getBoletos()) {
            listaItems.getItems().add("Boleto: " + b.getPelicula() + " — $" + String.format("%.2f", b.getPrecio() / 100.0));
            totalCentavos += b.getPrecio();
        }
        for (Producto p : carrito.getProductos()) {
            listaItems.getItems().add("Producto: " + p.getNombre() + " — $" + String.format("%.2f", p.getPrecio() / 100.0));
            totalCentavos += p.getPrecio();
        }
        if (lblTotal != null) lblTotal.setText("$ " + String.format("%.2f", totalCentavos / 100.0));
    }
}
