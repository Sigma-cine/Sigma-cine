package sigmacine.ui.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import sigmacine.aplicacion.data.UsuarioDTO;
import sigmacine.aplicacion.facade.AuthFacade;

public class LoginController {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    @FXML private Hyperlink registrarLink;
    @FXML private Label feedback;

    private ControladorControlador coordinador;
    private AuthFacade authFacade;

    public void setCoordinador(ControladorControlador coordinador) { this.coordinador = coordinador; }
    public void setAuthFacade(AuthFacade authFacade) { this.authFacade = authFacade; }

    @FXML
    private void initialize() {
        loginButton.setOnAction(e -> onLogin());
        registrarLink.setOnAction(e -> { if (coordinador != null) coordinador.mostrarRegistro(); });
    }
    @FXML
public void onIrARegistro() {
    coordinador.mostrarRegistro();
}


    public void onLogin() {
        String email = emailField.getText();
        String pass  = passwordField.getText();

        UsuarioDTO usuario = authFacade.login(email, pass);
        if (usuario == null) {
            feedback.setStyle("-fx-text-fill: #d00;");
            feedback.setText("Credenciales inválidas.");
            return;
        }

        feedback.setStyle("-fx-text-fill: #090;");
        feedback.setText("Bienvenido al Cine Sigma");
        if (coordinador != null) {
            // Después de autenticarse, mostrar la vista principal para el usuario
            coordinador.mostrarHome(usuario);
        }
    }
}
