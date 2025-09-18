package sigmacine.aplicacion.data;

public class UsuarioDTO {
    private Long id;
    private String email;
    private String rol;
    private String nombre;
    private String fechaRegistro; // solo cliente

    public Long getId() { return id; }
    public String getEmail() { return email; }
    public String getRol() { return rol; }
    public String getNombre() { return nombre; }
    public String getFechaRegistro() { return fechaRegistro; }

    public void setId(Long id) { this.id = id; }
    public void setEmail(String email) { this.email = email; }
    public void setRol(String rol) { this.rol = rol; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public void setFechaRegistro(String fechaRegistro) { this.fechaRegistro = fechaRegistro; }
}
