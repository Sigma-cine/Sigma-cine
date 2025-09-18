package sigmacine.aplicacion.service;

import java.util.Optional;

import sigmacine.aplicacion.data.UsuarioDTO;
import sigmacine.dominio.repository.UsuarioRepository;
import sigmacine.dominio.entity.*;
import sigmacine.dominio.valueobject.*;

public class LoginService {

    private final UsuarioRepository usuarios;

    public LoginService(UsuarioRepository usuarios) {
        this.usuarios = usuarios;
    }

    public UsuarioDTO autenticar(String emailRaw, String passwordRaw) {
        try {
            Email email = new Email(emailRaw);
            Optional<Usuario> opt = usuarios.buscarPorEmail(email);
            if (opt.isEmpty()) return null;

            Usuario u = opt.get();
            if (!u.autenticar(passwordRaw)) return null;

            UsuarioDTO dto = new UsuarioDTO();
            dto.setId(u.getId());
            dto.setEmail(u.getEmail().value());
            dto.setRol(u.getRol());

            if (u instanceof Admin a) {
                dto.setNombre(a.getNombre());
            } else if (u instanceof Cliente c) {
                dto.setNombre(c.getNombre());
                dto.setFechaRegistro(c.getFechaRegistro());
            }
            return dto;
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }
}
