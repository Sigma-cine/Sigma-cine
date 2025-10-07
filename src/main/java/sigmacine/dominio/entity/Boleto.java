package sigmacine.dominio.entity;

public class Boleto {
private int id;
    private Silla silla;
    private double precio;
    private String estado;

    public Boleto() {}

    public Boleto(int id, double precio, String estado) {
        this.id = id;
        this.silla = new Silla();
        this.precio = precio;
        this.estado = estado;
    }

    public int getId() { 
        return id; 
    }
    public void setId(int id) { 
        this.id = id; 
    }

    public Silla getSilla() { 
        return silla; 
    }
    public void setAsiento(Silla silla) { 
        this.silla = silla; 
    }

    public double getPrecio() { 
        return precio; 
    }
    public void setPrecio(double precio) { 
        this.precio = precio; 
    }

    public String getEstado() { 
        return estado; 
    }
    public void setEstado(String estado) { 
        this.estado = estado; 
    }
}
