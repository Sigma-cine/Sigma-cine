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

//Son temporales
import sigmacine.dominio.entity.*;//Estas import son solo para no tener que hacer login
import sigmacine.aplicacion.data.UsuarioDTO;// Por defecto se quemara un usuario y no se consumira informacion desde la base de datos


public class App extends Application {

    @Override
    public void start(Stage stage) {

        DatabaseConfig db = new DatabaseConfig();
        UsuarioRepository repo = new UsuarioRepositoryJdbc(db);
        LoginService loginService = new LoginService(repo);
        RegistroService registroService = new RegistroService(repo);    
        AuthFacade authFacade = new AuthFacade(loginService, registroService); 
       

        ControladorControlador coordinador = new ControladorControlador(stage, authFacade);
        coordinador.mostrarLogin();


       /*  //Este codigo es para omitir el login
           // Usuario u;
            UsuarioDTO dto = new UsuarioDTO();
            dto.setId(6L);
            dto.setEmail("ClientePrueba@sigma.com");

            //Aca el rol se definiria para las pantallas
           // dto.setRol("ADMIN");
           dto.setRol("");

                dto.setNombre("Equipo Sigma Cliente");
                dto.setFechaRegistro("25-09-2025");
            
            ControladorControlador coordinador=new ControladorControlador(stage, authFacade);
            coordinador.mostrarHome(dto);*/
    }

    public static void main(String[] args) {
        launch(args);
    }
}
