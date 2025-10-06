package sigmacine.dominio.entity;

public class Pago {
    private int id;
    private String metodo;
    private double monto;
    private String estado;
    private String fecha;

    public Pago() {}

    public Pago(int id, String metodo, double monto, String estado) {
        this.id = id;
        this.metodo = metodo;
        this.monto = monto;
        this.estado = estado;
    }

    public int getId() { 
        return id; 
    }
    public void setId(int id) { 
        this.id = id; 
    }

    public String getMetodo() { 
        return metodo; 
    }
    public void setMetodo (String metodoPago) { 
        this.metodo = metodoPago; 
    }

    public double getMonto() { 
        return monto; 
    }
    public void setMonto(double monto) { 
        this.monto = monto; 
    }

    public String getEstado() { 
        return estado; 
    }
    public void setEstado(String estado) { 
        this.estado = estado; 
    }

    public String getFecha() { 
        return fecha; 
    }
    public void setFecha(String fecha) { 
        this.fecha = fecha;
    }
}
