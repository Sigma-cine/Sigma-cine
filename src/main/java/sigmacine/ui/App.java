package sigmacine.ui;

import javafx.application.Application;
import javafx.stage.Stage;

import sigmacine.infraestructura.configDataBase.DatabaseConfig;
import sigmacine.infraestructura.persistencia.jdbc.UsuarioRepositoryJdbc;

import sigmacine.dominio.repository.UsuarioRepository;
import sigmacine.aplicacion.service.LoginService;
import sigmacine.aplicacion.facade.AuthFacade;
import sigmacine.ui.controller.ControladorControlador;
import sigmacine.aplicacion.service.RegistroService;

public class App extends Application {

    @Override
    public void start(Stage stage) {
       /*DatabaseConfig db = new DatabaseConfig();
        UsuarioRepository repo = new UsuarioRepositoryJdbc(db);
        LoginService loginService = new LoginService(repo);
        AuthFacade authFacade = new AuthFacade(loginService);*/

        DatabaseConfig db = new DatabaseConfig();
        UsuarioRepository repo = new UsuarioRepositoryJdbc(db);
        LoginService loginService = new LoginService(repo);
        RegistroService registroService = new RegistroService(repo);    
        AuthFacade authFacade = new AuthFacade(loginService, registroService); 
       
        ControladorControlador coordinador = new ControladorControlador(stage, authFacade);
        coordinador.mostrarLogin();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
