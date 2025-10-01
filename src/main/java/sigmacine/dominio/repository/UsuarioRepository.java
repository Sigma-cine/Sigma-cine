package sigmacine.dominio.repository;

import java.util.Optional;
import sigmacine.dominio.entity.Usuario;
import sigmacine.dominio.valueobject.Email;
import sigmacine.dominio.valueobject.PasswordHash;

public interface UsuarioRepository {
    //Optional<Usuario> buscarPorEmail(Email email);
    void guardar(Usuario usuario);
    int crearCliente(Email email, PasswordHash passwordHash, String nombre);
    Usuario buscarPorEmail(Email email);

}
