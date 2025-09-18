// RegisterController.java
package sigmacine.ui.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import sigmacine.aplicacion.facade.AuthFacade;

public class RegisterController {

    @FXML private TextField txtNombre;
    @FXML private TextField txtEmail;
    @FXML private PasswordField txtPassword;

    private final AuthFacade auth;
    private ControladorControlador coordinador; // <- se inyecta por setter

    public RegisterController(AuthFacade auth) {
        this.auth = auth;
    }

    public void setCoordinador(ControladorControlador c) {
        this.coordinador = c;
    }

    @FXML
    public void onRegistrar() {
        String nombre = txtNombre.getText().trim();
        String email  = txtEmail.getText().trim();
        String pass   = txtPassword.getText();

        try {
            if (nombre.isEmpty() || email.isEmpty() || pass.isEmpty()) {
                throw new IllegalArgumentException("Completa todos los campos");
            }
            Long id = auth.registrar(nombre, email, pass);

            new Alert(Alert.AlertType.INFORMATION,
                "¡Registro exitoso! ID = " + id, ButtonType.OK).showAndWait();

            if (coordinador != null) coordinador.mostrarLogin(); // volver al login
        } catch (IllegalArgumentException ex) {
            new Alert(Alert.AlertType.WARNING, ex.getMessage(), ButtonType.OK).showAndWait();
        } catch (Exception ex) {
            new Alert(Alert.AlertType.ERROR, "Error registrando: " + ex.getMessage(), ButtonType.OK).showAndWait();
        }
    }

    @FXML
    public void onCancelar() {
        if (coordinador != null) coordinador.mostrarLogin();
    }
}
