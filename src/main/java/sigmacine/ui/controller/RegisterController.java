// RegisterController.java
package sigmacine.ui.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import sigmacine.aplicacion.facade.AuthFacade;


public class RegisterController {

    @FXML private TextField txtNombre;
    @FXML private TextField txtEmail;
    @FXML private PasswordField txtPassword;
    @FXML private Label feedback;

    private final AuthFacade auth;
    private ControladorControlador coordinador; 

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

           /* new Alert(Alert.AlertType.INFORMATION,"¡Registro exitoso! ID = " + id, ButtonType.OK).showAndWait();*/
            
               feedback.setStyle("-fx-text-fill: #2e7d32;");  // verde éxito
              feedback.setText("¡Registro exitoso! ID = " + id);

            if (coordinador != null) coordinador.mostrarLogin(); 
        } catch (IllegalArgumentException ex) {
           /*new Alert(Alert.AlertType.WARNING, ex.getMessage(), ButtonType.OK).showAndWait();*/
                    feedback.setStyle("-fx-text-fill: #e53935;");  // rojo error
                    feedback.setText(ex.getMessage());
        } catch (Exception ex) {
            /*new Alert(Alert.AlertType.ERROR, "Error registrando: " + ex.getMessage(), ButtonType.OK).showAndWait();*/
             feedback.setStyle("-fx-text-fill: #e53935;");
             feedback.setText("Error registrando: " + ex.getMessage());
    }
}

    @FXML
    public void onCancelar() {
        if (coordinador != null) coordinador.mostrarLogin();
    }
}
