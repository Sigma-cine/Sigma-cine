package sigmacine.aplicacion.service;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import sigmacine.aplicacion.data.CompraProductoDTO;

import java.math.BigDecimal;
import java.util.List;

public class CarritoService {
    private static final CarritoService INSTANCE = new CarritoService();

    private final ObservableList<CompraProductoDTO> items = FXCollections.observableArrayList();

    private CarritoService() {}

    public static CarritoService getInstance() { return INSTANCE; }

    public ObservableList<CompraProductoDTO> getItems() { return items; }

    public void addItem(CompraProductoDTO item) {
        if (item == null) return;
        items.add(item);
    }

    public void removeItem(CompraProductoDTO item) { items.remove(item); }

    public void clear() { items.clear(); }

    public BigDecimal getTotal() {
        return items.stream()
                .map(i -> i.getPrecioUnitario().multiply(java.math.BigDecimal.valueOf(i.getCantidad())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public void addListener(ListChangeListener<? super CompraProductoDTO> l) { items.addListener(l); }
}
