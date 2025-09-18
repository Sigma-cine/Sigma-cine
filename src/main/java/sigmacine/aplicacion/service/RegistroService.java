package sigmacine.aplicacion.service;

import org.mindrot.jbcrypt.BCrypt;
import sigmacine.dominio.repository.UsuarioRepository;
import sigmacine.dominio.valueobject.Email;
import sigmacine.dominio.valueobject.PasswordHash;

public class RegistroService {
    private final UsuarioRepository repo;

    public RegistroService(UsuarioRepository repo) { this.repo = repo; }

    public Long registrarCliente(String nombre, String emailPlano, String contrasenaPlano) {
        Email email = new Email(emailPlano);

        // 1) Unicidad de email
        if (repo.buscarPorEmail(email).isPresent()) {
            throw new IllegalArgumentException("El email ya está registrado");
        }

        // 2) Hash de contraseña
        String hash = BCrypt.hashpw(contrasenaPlano, BCrypt.gensalt(10));
        PasswordHash ph = new PasswordHash(hash);

        // 3) Crear en BD
        return repo.crearCliente(email, ph, nombre);
    }
}
