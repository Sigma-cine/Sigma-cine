package sigmacine.ui.controller;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import sigmacine.aplicacion.data.UsuarioDTO;
import sigmacine.aplicacion.facade.AuthFacade;

public class ControladorControlador {

    private final Stage stage;
    private final AuthFacade authFacade;

    public ControladorControlador(Stage stage, AuthFacade authFacade) {
        this.stage = stage;
        this.authFacade = authFacade;
    }

    public void mostrarLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/sigmacine/ui/views/login.fxml"));
            Parent root = loader.load();
            LoginController controller = loader.getController();
            controller.setCoordinador(this);
            controller.setAuthFacade(authFacade);

            stage.setTitle("Sigma Cine - Login");
            javafx.scene.Scene current = stage.getScene();
            double w = current != null ? current.getWidth() : 900;
            double h = current != null ? current.getHeight() : 600;
            stage.setScene(new Scene(root, w > 0 ? w : 900, h > 0 ? h : 600));
            stage.show();
        } catch (Exception e) {
            throw new RuntimeException("Error cargando login.fxml", e);
        }
    }

    public void mostrarRegistro() {
    try {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/sigmacine/ui/views/registrarse.fxml"));

        // Instanciamos el controller pasando AuthFacade
        RegisterController controller = new RegisterController(authFacade);
        controller.setCoordinador(this); // pásale el coordinador
        loader.setController(controller); // <-- clave

    Parent root = loader.load();
    stage.setTitle("Sigma Cine - Registrarse");
    javafx.scene.Scene current = stage.getScene();
    double w = current != null ? current.getWidth() : 900;
    double h = current != null ? current.getHeight() : 600;
    stage.setScene(new Scene(root, w > 0 ? w : 900, h > 0 ? h : 600));
    stage.show();
    } catch (Exception e) {
        throw new RuntimeException("Error cargando registrarse.fxml", e);
    }
}


    /** Compatibilidad hacia atrás: ahora delega al home por rol */
    public void mostrarBienvenida(UsuarioDTO usuario) {
        mostrarHome(usuario);
    }

    /** Nuevo: enruta por rol y carga el controlador correspondiente */
    public void mostrarHome(UsuarioDTO usuario) {
        try {
        boolean esAdmin = "ADMIN".equalsIgnoreCase(usuario.getRol());
        String fxml = esAdmin
            ? "/sigmacine/ui/views/admin_dashboard.fxml"
            : "/sigmacine/ui/views/cliente_home.fxml";
                    

            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            Parent root = loader.load();

            if (esAdmin) {
                AdminController c = loader.getController();
                c.init(usuario, this);
                stage.setTitle("Sigma Cine - Admin");
            } else {
                ClienteController c = loader.getController();
                c.initCiudad(usuario);
                c.setCoordinador(this);
                stage.setTitle("Sigma Cine - Cliente");
            }

            // Mensaje de bienvenida estándar (si la vista tiene el label)
            Label lbl = (Label) root.lookup("#welcomeLabel");
            if (lbl != null) lbl.setText("Bienvenido al Cine Sigma");

            javafx.scene.Scene current = stage.getScene();
            double w = current != null ? current.getWidth() : 900;
            double h = current != null ? current.getHeight() : 600;
            stage.setScene(new Scene(root, w > 0 ? w : 900, h > 0 ? h : 600));
            stage.show();
        } catch (Exception e) {
            throw new RuntimeException("Error cargando vista home por rol", e);
        }
    }

    /** Busca películas por título y muestra la vista de resultados de búsqueda */
    public void mostrarResultadosBusqueda(String texto) {
        try {
            // crear repo temporal para búsqueda
            sigmacine.infraestructura.configDataBase.DatabaseConfig db = new sigmacine.infraestructura.configDataBase.DatabaseConfig();
            var repo = new sigmacine.infraestructura.persistencia.jdbc.PeliculaRepositoryJdbc(db);
            var resultados = repo.buscarPorTitulo(texto != null ? texto : "");

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/sigmacine/ui/views/resultados_busqueda.fxml"));
            Parent root = loader.load();
            ResultadosBusquedaController controller = loader.getController();
            controller.setResultados(resultados, texto != null ? texto : "");

            stage.setTitle("Resultados de búsqueda");
            javafx.scene.Scene current = stage.getScene();
            double w = current != null ? current.getWidth() : 900;
            double h = current != null ? current.getHeight() : 600;
            stage.setScene(new Scene(root, w > 0 ? w : 900, h > 0 ? h : 600));
            stage.show();
        } catch (Exception e) {
            throw new RuntimeException("Error mostrando resultados de búsqueda", e);
        }
    }

    /** Carga la vista cliente_home y abre un popup modal para seleccionar la ciudad al inicio */
    public void mostrarClienteHomeConPopup(UsuarioDTO usuario) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/sigmacine/ui/views/cliente_home.fxml"));
            Parent root = loader.load();

            ClienteController cliente = loader.getController();
            cliente.init(usuario);
            cliente.setCoordinador(this);

            javafx.scene.Scene current = stage.getScene();
            double w = current != null ? current.getWidth() : 900;
            double h = current != null ? current.getHeight() : 600;
            stage.setScene(new Scene(root, w > 0 ? w : 900, h > 0 ? h : 600));
            stage.setTitle("Sigma Cine - Cliente");
            stage.show();

            // Ahora carga el popup de ciudad como modal
            FXMLLoader popup = new FXMLLoader(getClass().getResource("/sigmacine/ui/views/ciudad.fxml"));
            Parent popupRoot = popup.load();
            CiudadController cc = popup.getController();
            cc.setOnCiudadSelected(ciudad -> {
                // cuando se seleccione la ciudad, informa al cliente
                cliente.init(usuario, ciudad);
                stage.setTitle("Sigma Cine - Cliente (" + ciudad + ")");
            });

            Stage dialog = new Stage();
            dialog.initOwner(stage);
            dialog.initModality(javafx.stage.Modality.WINDOW_MODAL);
            dialog.setTitle("Seleccione su ciudad");
            dialog.setScene(new javafx.scene.Scene(popupRoot));
            dialog.setResizable(false);
            dialog.centerOnScreen();
            dialog.showAndWait();

        } catch (Exception e) {
            throw new RuntimeException("Error cargando cliente_home con popup de ciudad", e);
        }
    }

    //================================================
   /*  public void mostrarMisCompras() {
    try {
        var loader = new FXMLLoader(getClass().getResource("/sigmacine/ui/views/mis_compras.fxml"));
        var root = loader.load();
        // si tienes controlador: MisComprasController c = loader.getController(); c.init(...);
        stage.setTitle("Sigma Cine - Mis compras");
        stage.setScene(new Scene(root));
        stage.show();
    } catch (Exception e) {
        throw new RuntimeException("Error cargando mis_compras.fxml", e);
    }
    public void mostrarCartelera() {
    try {
        var loader = new javafx.fxml.FXMLLoader(getClass().getResource("/sigmacine/ui/views/cartelera.fxml"));
        var root = loader.load();
        var c = loader.getController();
        if (c instanceof sigmacine.ui.controller.CarteleraController cc) cc.setCoordinador(this);
        stage.setTitle("Sigma Cine - Cartelera");
        stage.setScene(new javafx.scene.Scene(root));
        stage.show();
    } catch (Exception e) {
        throw new RuntimeException("Error cargando cartelera.fxml", e);
    }
}

public void mostrarConfiteria() {
    try {
        var loader = new javafx.fxml.FXMLLoader(getClass().getResource("/sigmacine/ui/views/confiteria.fxml"));
        var root = loader.load();
        var c = loader.getController();
        if (c instanceof sigmacine.ui.controller.ConfiteriaController cc) cc.setCoordinador(this);
        stage.setTitle("Sigma Cine - Confitería");
        stage.setScene(new javafx.scene.Scene(root));
        stage.show();
    } catch (Exception e) {
        throw new RuntimeException("Error cargando confiteria.fxml", e);
    }
}
*/
}