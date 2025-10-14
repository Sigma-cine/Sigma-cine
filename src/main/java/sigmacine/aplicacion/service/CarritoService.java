package sigmacine.aplicacion.service;

import sigmacine.dominio.entity.Boleto;
import sigmacine.dominio.entity.Producto;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Servicio sencillo en memoria para gestionar el carrito de compras. */
public class CarritoService {
    private static final CarritoService INSTANCE = new CarritoService();

    private final List<Boleto> boletos = new ArrayList<>();
    private final List<Producto> productos = new ArrayList<>();

    private CarritoService() {}

    public static CarritoService getInstance() { return INSTANCE; }

    public synchronized void addBoleto(Boleto b) {
        if (b == null) throw new IllegalArgumentException("Boleto nulo");
        boletos.add(b);
    }

    public synchronized void addProducto(Producto p) {
        if (p == null) throw new IllegalArgumentException("Producto nulo");
        productos.add(p);
    }

    public synchronized List<Boleto> getBoletos() { return Collections.unmodifiableList(new ArrayList<>(boletos)); }

    public synchronized List<Producto> getProductos() { return Collections.unmodifiableList(new ArrayList<>(productos)); }

    public synchronized void clear() { boletos.clear(); productos.clear(); }
}
