package sigmacine.dominio.entity;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class Compra {
    private Long id;
   // private Cliente cliente;
    //private List<Boleto> boletos = new ArrayList<>();
    //private List<Producto> productos = new ArrayList<>();
    //private Dinero total = new Dinero(BigDecimal.ZERO);
    //private Pago pago;

    // Constructor que inicializa una nueva compra con un cliente
   /* public Compra(Long id, Cliente cliente) {
        this.id = id;
        this.cliente = cliente;
    }*/

   /*  public void agregarBoleto(Boleto boleto) {
        if (boleto == null) {
            throw new IllegalArgumentException("El boleto no puede ser nulo.");
        }
        this.boletos.add(boleto);
        this.total.sumar(boleto.getPrecio());
    }*/

    /*public void agregarProducto(Producto producto) {
        if (producto == null) {
            throw new IllegalArgumentException("El producto no puede ser nulo.");
        }
        this.productos.add(producto);
        this.total.sumar(producto.getPrecio());
    }*/
    
    // Método para asociar un pago a la compra
    /*public void setPago(Pago pago) {
        if (pago == null) {
            throw new IllegalArgumentException("El pago no puede ser nulo.");
        }
        this.pago = pago;
    }*/

    // Getters
    public Long getId() {
        return id;
    }

 /*   public Cliente getCliente() {
        return cliente;
    }

   /*  public Dinero getTotal() {
        return total;
    }

    public List<Boleto> getBoletos() {
        return boletos;
    }
    
    public List<Producto> getProductos() {
        return productos;
    }
    
    public Pago getPago() {
        return pago;
    }*/
}