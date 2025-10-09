package sigmacine.dominio.entity;

public class Producto {
    private int id;
    private String nombre;
    private String descripcion;
    private double precio;
    private String tipo;
    private String estado;

    public Producto() {}

    public Producto(int id, String nombre, String descripcion, double precio, String estado, String tipo) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precio = precio;
        this.tipo = tipo;
        this.estado = estado;
    }

    public int getId() { 
        return id; 
    }
    public void setId(int id) { 
        this.id = id; 
    }

    public String getNombre() { 
        return nombre; 
    }
    public void setNombre(String nombre) { 
        this.nombre = nombre; 
    }

    public String getDescripcion() { 
        return descripcion; 
    }
    public void setDescripcion(String descripcion) { 
        this.descripcion = descripcion; 
    }

    public double getPrecio() { 
        return precio; 
    }
    public void setPrecio(double precio) { 
        this.precio = precio; 
    }

    public String getCategoria() { 
        return tipo; 
    }
    public void setTipo(String tipo) { 
        this.tipo = tipo; 
    }

    public String getEstado() { 
        return estado; 
    }
    public void setEstado(String estado) { 
        this.estado = estado; 
    }
}