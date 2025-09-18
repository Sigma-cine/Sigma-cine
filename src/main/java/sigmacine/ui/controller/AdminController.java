package sigmacine.ui.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import sigmacine.aplicacion.data.UsuarioDTO;

public class AdminController {

    @FXML private Label welcomeLabel;

    private UsuarioDTO usuario;
    private ControladorControlador coordinador;

    public void init(UsuarioDTO usuario, ControladorControlador coordinador) {
        this.usuario = usuario;
        this.coordinador = coordinador;
        if (welcomeLabel != null) welcomeLabel.setText("Bienvenido al Cine Sigma");
    }

    @FXML
    private void onLogout() {
        if (coordinador != null) coordinador.mostrarLogin();
    }

    // Aquí irán acciones del admin (gestionar funciones, productos, reportes…)
}
