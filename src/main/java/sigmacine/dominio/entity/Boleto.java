package sigmacine.dominio.entity;

public class Boleto {
    private Long id;
    private String pelicula;
    private String sala;
    private String horario;
    private String asiento;
    private long precio;


    public String getPelicula() {
        return pelicula;
    }

    public String getSala() {
        return sala;
    }

    public String getHorario() {
        return horario;
    }
    
    public long getPrecio() {
        return precio;
    }
}