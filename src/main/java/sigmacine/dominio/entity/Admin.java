package sigmacine.dominio.entity;

import sigmacine.dominio.valueobject.Email;
import sigmacine.dominio.valueobject.PasswordHash;

public class Admin extends Usuario {
    private String nombre;

    public Admin(Long id, Email email, PasswordHash passwordHash, String nombre) {
        super(id, email, passwordHash, Usuario.ROL_ADMIN);
        this.nombre = nombre;
    }

    public String getNombre() { return nombre; }
}
