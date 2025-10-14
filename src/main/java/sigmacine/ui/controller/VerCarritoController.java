package sigmacine.ui.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import javafx.stage.Modality;
import javafx.scene.Scene;
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

    private void navegarAPantallaPago() {
        if (carrito.getItems().isEmpty()) {
            javafx.scene.control.Alert a = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION, "El carrito está vacío.");
            a.setHeaderText(null);
            a.setTitle("Carrito vacío");
            a.showAndWait();
            return;
        }
        // Nueva implementación programática de la ventana de pago
        Stage stage = new Stage();
        stage.setTitle("Pago");
        stage.initModality(Modality.APPLICATION_MODAL);

        ListView<CompraProductoDTO> lista = new ListView<>(carrito.getItems());
        lista.setPrefHeight(200);

        Label lblTotalPago = new Label();
        lblTotalPago.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");

        Runnable refreshTotal = () -> lblTotalPago.setText("$" + carrito.getTotal().toPlainString());
        refreshTotal.run();
        carrito.addListener(c -> refreshTotal.run());

        // Método de pago
        ToggleGroup tg = new ToggleGroup();
        RadioButton rbCredito = new RadioButton("Tarjeta de crédito");
        rbCredito.setToggleGroup(tg); rbCredito.setSelected(true); rbCredito.setStyle("-fx-text-fill:#ddd;");
        RadioButton rbDebito = new RadioButton("Tarjeta débito");
        rbDebito.setToggleGroup(tg); rbDebito.setStyle("-fx-text-fill:#ddd;");

        Button btnPagar = new Button("Pagar");
        btnPagar.setDefaultButton(true);
        btnPagar.setStyle("-fx-background-color:#8B2E21; -fx-text-fill:white; -fx-font-weight:bold; -fx-background-radius:18; -fx-padding:8 22;");
        btnPagar.setOnAction(e -> {
            Alert ok = new Alert(Alert.AlertType.INFORMATION, "Compra realizada con éxito. ¡Disfruta tu película!");
            ok.setHeaderText(null);
            ok.setTitle("Pago exitoso");
            ok.showAndWait();
            stage.close();
        });

        HBox lineaTotal = new HBox();
        Label lblTotalTxt = new Label("TOTAL");
        lblTotalTxt.setStyle("-fx-text-fill:white; -fx-font-weight:bold;");
        Region spacer = new Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
        lineaTotal.getChildren().addAll(lblTotalTxt, spacer, lblTotalPago);
        lineaTotal.setSpacing(8);

        VBox root = new VBox(14,
                new Label("Resumen de compra") {{ setStyle("-fx-text-fill:white; -fx-font-size:18; -fx-font-weight:bold;"); }},
                lista,
                lineaTotal,
                new Label("Método de pago") {{ setStyle("-fx-text-fill:#bbb; -fx-font-weight:bold;"); }},
                new VBox(6, rbCredito, rbDebito),
                btnPagar
        );
        root.setStyle("-fx-background-color:#000; -fx-padding:24;");

        stage.setScene(new Scene(root, 480, 520));
        stage.showAndWait();
    }


    // Handler invocado desde el FXML (onAction="#onProcederPago")
    @FXML
    private void onProcederPago() {
        navegarAPantallaPago();
    }
}
