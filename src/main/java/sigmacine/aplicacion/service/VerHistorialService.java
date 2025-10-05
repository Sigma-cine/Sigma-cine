package sigmacine.aplicacion.service;
import java.util.List;

import sigmacine.dominio.entity.Compra;
import sigmacine.dominio.entity.Usuario;
import sigmacine.dominio.repository.UsuarioRepository;
import sigmacine.dominio.valueobject.Email;

public class VerHistorialService {
    public final UsuarioRepository repo;
    
    public VerHistorialService(UsuarioRepository repo) { this.repo = repo; }

    public List<Compra> verHistorial(String emailPlano) {
        Email email = new Email(emailPlano);
        Usuario usuario = repo.buscarPorEmail(email);
        if (usuario == null) {
            throw new IllegalArgumentException("El email no está registrado");
        }
        return usuario.getCompras();
    }
    
}
