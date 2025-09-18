package sigmacine.aplicacion.facade;

import sigmacine.aplicacion.data.UsuarioDTO;
import sigmacine.aplicacion.service.LoginService;
import sigmacine.aplicacion.service.RegistroService;
public class AuthFacade {

    private final LoginService loginService;
     private final RegistroService registroService;

    /*public AuthFacade(LoginService loginService) {
        this.loginService = loginService;
    }

    public UsuarioDTO login(String email, String password) {
        return loginService.autenticar(email, password);
    }
    public AuthFacade(LoginService loginService, RegistroService registroService) {
        this.loginService = loginService;
        this.registroService = registroService;
    }

    public Long registrar(String nombre, String email, String pass) {
        return registroService.registrarCliente(nombre, email, pass);
    }*/
    public AuthFacade(LoginService loginService) {
        this(loginService, null);
    }

    // Nuevo: cuando quieres habilitar registro
    public AuthFacade(LoginService loginService, RegistroService registroService) {
        this.loginService = loginService;
        this.registroService = registroService;
    }

    public UsuarioDTO login(String email, String pass) {
        return loginService.autenticar(email, pass);
    }

    public Long registrar(String nombre, String email, String pass) {
        if (registroService == null)
            throw new IllegalStateException("Registro no disponible (no se pasó RegistroService).");
        return registroService.registrarCliente(nombre, email, pass);
        
    }
}
