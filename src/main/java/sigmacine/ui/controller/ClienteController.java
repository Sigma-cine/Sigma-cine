package sigmacine.ui.controller;

import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.MenuButton;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;

import sigmacine.aplicacion.data.UsuarioDTO;
import sigmacine.aplicacion.service.VerHistorialService;
import sigmacine.aplicacion.session.Session;
import sigmacine.dominio.entity.Pelicula;
import sigmacine.infraestructura.configDataBase.DatabaseConfig;
import sigmacine.infraestructura.persistencia.jdbc.PeliculaRepositoryJdbc;
import sigmacine.infraestructura.persistencia.jdbc.UsuarioRepositoryJdbc;

import java.util.List;

/**
 * Controlador principal del cliente (pagina_inicial.fxml).
 */
public class ClienteController {

    @FXML private Button btnPromoVerMas;
    @FXML private Button btnCartelera;
    @FXML private Button btnConfiteria;
    @FXML private Button btnSigmaCard;
    @FXML private Button btnCart;
    @FXML private MenuItem miCerrarSesion;
    @FXML private MenuItem miHistorial;
    @FXML private MenuButton menuPerfil;

    @FXML private javafx.scene.layout.StackPane promoPane;
    @FXML private ImageView imgPublicidad;
    @FXML private Label lblPromo;

    @FXML private TextField txtBuscar;
    @FXML private Button btnBuscar;

    @FXML private javafx.scene.layout.StackPane content;

    @FXML private ChoiceBox<String> cbCiudad;
    @FXML private Button btnSeleccionarCiudad;
    @FXML private Button btnIniciarSesion;
    @FXML private Label  lblUserName;
    @FXML private Button btnRegistrarse;

    @FXML private javafx.scene.layout.GridPane footerGrid;
    @FXML private ImageView imgCard1;
    @FXML private Label lblCard1;
    @FXML private ImageView imgCard2;
    @FXML private Label lblCard2;
    @FXML private ImageView imgCard3;
    @FXML private Label lblCard3;
    @FXML private ImageView imgCard4;
    @FXML private Label lblCard4;

    private UsuarioDTO usuario;
    private String ciudadSeleccionada;
    private ControladorControlador coordinador;

    public void setCoordinador(ControladorControlador c) { 
        this.coordinador = c; 
    }

    public void init(UsuarioDTO usuario) {
        this.usuario = usuario;
        esperarYCargarPeliculas();
        refreshSessionUI();
    }

    public void init(UsuarioDTO usuario, String ciudad) {
        this.usuario = usuario;
        this.ciudadSeleccionada = ciudad;
        esperarYCargarPeliculas();
        refreshSessionUI();
    }

    public void initCiudad(UsuarioDTO usuario) {
        this.usuario = usuario;
        if (cbCiudad != null) {
            cbCiudad.getItems().setAll("Bogot�", "Medell�n", "Cali", "Barranquilla");
            cbCiudad.getSelectionModel().selectFirst();
        }
        refreshSessionUI();
    }

    @FXML
    private void initialize() {
        if (promoPane != null && imgPublicidad != null) {
            imgPublicidad.fitWidthProperty().bind(promoPane.widthProperty());
            imgPublicidad.setFitHeight(110);
            imgPublicidad.setPreserveRatio(true);
        }

        if (btnSeleccionarCiudad != null) btnSeleccionarCiudad.setOnAction(e -> onSeleccionarCiudad());
        if (btnCartelera != null)        btnCartelera.setOnAction(e -> mostrarCartelera());
        if (btnConfiteria != null)       btnConfiteria.setOnAction(e -> System.out.println("Ir a Confiter�a (" + safeCiudad() + ")"));
        if (miCerrarSesion != null)      miCerrarSesion.setOnAction(e -> onLogout());
        if (miHistorial != null)         miHistorial.setOnAction(e -> onVerHistorial());

        updateMenuPerfilState();

        if (btnIniciarSesion != null) btnIniciarSesion.setOnAction(e -> onIniciarSesion());
        if (btnRegistrarse  != null) {
            btnRegistrarse.setOnAction(e -> onRegistrarse());
            btnRegistrarse.setDisable(Session.isLoggedIn());
        }

        if (txtBuscar != null) {
            txtBuscar.setOnKeyPressed(ev -> { if (ev.getCode() == KeyCode.ENTER) doSearch(txtBuscar.getText()); });
            if (btnBuscar != null) btnBuscar.setOnAction(e -> doSearch(txtBuscar.getText()));
        }

        if (content != null) {
            content.getChildren().addListener((ListChangeListener<Node>) c -> updatePublicidadVisibility());
            updatePublicidadVisibility();
        }

    }

    private void esperarYCargarPeliculas() {
        if (btnCartelera != null && btnCartelera.getScene() != null) {
            Platform.runLater(this::cargarPeliculasInicio);
        } else if (btnCartelera != null) {
            btnCartelera.sceneProperty().addListener((obs, oldS, newS) -> {
                if (newS != null) Platform.runLater(this::cargarPeliculasInicio);
            });
        } else {
            new Thread(() -> {
                try {
                    Thread.sleep(800);
                    Platform.runLater(this::cargarPeliculasInicio);
                } catch (InterruptedException ignored) {}
            }).start();
        }
    }

    private void cargarPeliculasInicio() {
        try {
            DatabaseConfig db = new DatabaseConfig();
            PeliculaRepositoryJdbc repo = new PeliculaRepositoryJdbc(db);
            List<Pelicula> peliculas = repo.buscarPorTitulo(""); // todas

            if (peliculas == null || peliculas.isEmpty()) return;

            if (footerGrid == null) {
                footerGrid = localizarFooterGridDesdeRoot();
                if (footerGrid == null) return;
            }
            renderizarFooterDinamico(footerGrid, peliculas);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private javafx.scene.layout.GridPane localizarFooterGridDesdeRoot() {
        try {
            Scene sc = null;
            if (promoPane != null) sc = promoPane.getScene();
            if (sc == null && content != null) sc = content.getScene();
            if (sc == null && imgPublicidad != null) sc = imgPublicidad.getScene();
            if (sc == null && btnBuscar != null) sc = btnBuscar.getScene();
            if (sc == null) return null;

            Parent root = sc.getRoot();
            if (root instanceof javafx.scene.layout.BorderPane bp) {
                Node bottom = bp.getBottom();
                if (bottom instanceof javafx.scene.layout.HBox hb) {
                    for (Node ch : hb.getChildren()) {
                        if (ch instanceof javafx.scene.layout.GridPane gp) return gp;
                    }
                }
            }
        } catch (Exception ignored) {}
        return null;
    }

    private void renderizarFooterDinamico(javafx.scene.layout.GridPane grid, List<Pelicula> peliculas) {
        try {
            grid.getChildren().clear();
            if (grid.getColumnConstraints() != null) grid.getColumnConstraints().clear();
            if (grid.getRowConstraints() != null) grid.getRowConstraints().clear();

            grid.setMaxWidth(javafx.scene.layout.Region.USE_PREF_SIZE);
            grid.setPrefWidth(javafx.scene.layout.Region.USE_COMPUTED_SIZE);
            grid.setPadding(new javafx.geometry.Insets(12, 64, 12, 64));
            grid.setHgap(64);
            grid.setVgap(16);
            grid.setAlignment(javafx.geometry.Pos.CENTER);
            grid.setTranslateY(-30);

            int max = Math.min(3, peliculas.size());
            for (int i = 0; i < max; i++) {
                Pelicula p = peliculas.get(i);
                int col = i;

                ImageView poster = new ImageView();
                poster.setPreserveRatio(true);
                poster.setFitWidth(220);
                if (p.getPosterUrl() != null && !p.getPosterUrl().isBlank()) {
                    Image img = resolveImage(p.getPosterUrl());
                    if (img != null) poster.setImage(img);
                }
                javafx.scene.layout.GridPane.setColumnIndex(poster, col);
                javafx.scene.layout.GridPane.setRowIndex(poster, 0);
                javafx.scene.layout.GridPane.setMargin(poster, new javafx.geometry.Insets(0, 0, 8, 0));
                grid.getChildren().add(poster);

                Label titulo = new Label(p.getTitulo() != null ? p.getTitulo() : "Sin t�tulo");
                titulo.setWrapText(true);
                titulo.setMaxWidth(220);
                titulo.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 16;");
                titulo.setAlignment(javafx.geometry.Pos.CENTER);
                titulo.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
                javafx.scene.layout.GridPane.setColumnIndex(titulo, col);
                javafx.scene.layout.GridPane.setRowIndex(titulo, 1);
                javafx.scene.layout.GridPane.setHalignment(titulo, javafx.geometry.HPos.CENTER);
                javafx.scene.layout.GridPane.setMargin(titulo, new javafx.geometry.Insets(0, 0, 8, 0));
                grid.getChildren().add(titulo);

                Button verMas = new Button("Ver m�s");
                verMas.setStyle("-fx-background-color: #993726; -fx-background-radius: 14; -fx-font-weight: bold; -fx-text-fill: #ffffff;");
                verMas.setPrefWidth(96);
                verMas.setPrefHeight(34);
                javafx.scene.layout.GridPane.setColumnIndex(verMas, col);
                javafx.scene.layout.GridPane.setRowIndex(verMas, 2);
                javafx.scene.layout.GridPane.setHalignment(verMas, javafx.geometry.HPos.CENTER);
                grid.getChildren().add(verMas);
            }

            Platform.runLater(() -> { try { grid.applyCss(); grid.layout(); } catch (Exception ignored) {} });
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private ImageView buscarImageView(String fxId) {
        try {
            if (btnCartelera != null && btnCartelera.getScene() != null) {
                return (ImageView) btnCartelera.getScene().lookup(fxId);
            }
            if (content != null && content.getScene() != null) {
                return (ImageView) content.getScene().lookup(fxId);
            }
        } catch (Exception ex) { ex.printStackTrace(); }
        return null;
    }
    private Label buscarLabel(String fxId) {
        try {
            if (btnCartelera != null && btnCartelera.getScene() != null) {
                return (Label) btnCartelera.getScene().lookup(fxId);
            }
            if (content != null && content.getScene() != null) {
                return (Label) content.getScene().lookup(fxId);
            }
        } catch (Exception ex) { ex.printStackTrace(); }
        return null;
    }

    private void cargarCard(ImageView img, Label lbl, Pelicula pelicula) {
        if (pelicula == null) return;
        if (lbl != null) lbl.setText(pelicula.getTitulo() != null ? pelicula.getTitulo() : "Sin t�tulo");
        if (img != null && pelicula.getPosterUrl() != null && !pelicula.getPosterUrl().isBlank()) {
            Image posterImage = resolveImage(pelicula.getPosterUrl());
            if (posterImage != null) img.setImage(posterImage);
        }
    }

    private Image resolveImage(String ref) {
        if (ref == null || ref.isBlank()) return null;
        try {
            String lower = ref.toLowerCase();
            if (lower.startsWith("http://") || lower.startsWith("https://") || lower.startsWith("file:/")) {
                return new Image(ref, true);
            }
            if (ref.contains("src\\main\\resources\\Images\\") || ref.contains("src/main/resources/Images/")) {
                String fileName = ref.substring(Math.max(ref.lastIndexOf('\\'), ref.lastIndexOf('/')) + 1);
                java.net.URL res = getClass().getResource("/Images/" + fileName);
                if (res != null) return new Image(res.toExternalForm(), false);
            }
            java.net.URL res2 = getClass().getResource("/Images/" + ref);
            if (res2 != null) return new Image(res2.toExternalForm(), false);

            java.io.File f = new java.io.File(ref);
            if (f.exists()) return new Image(f.toURI().toString(), false);

            java.net.URL res3 = getClass().getResource(ref.startsWith("/") ? ref : ("/" + ref));
            if (res3 != null) return new Image(res3.toExternalForm(), false);
        } catch (Exception ignored) {}
        return null;
    }

    private void onIniciarSesion() {
        if (Session.isLoggedIn()) {
            var a = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.CONFIRMATION);
            a.setTitle("Cerrar sesi�n");
            a.setHeaderText("�Desea cerrar sesi�n?");
            a.setContentText("Salir de la cuenta " + (Session.getCurrent() != null ? Session.getCurrent().getEmail() : ""));
            var opt = a.showAndWait();
            if (opt.isPresent() && opt.get() == javafx.scene.control.ButtonType.OK) onLogout();
            return;
        }
        if (coordinador != null) coordinador.mostrarLogin();
    }

    private void onRegistrarse() {
        if (Session.isLoggedIn()) {
            var a = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
            a.setTitle("Ya has iniciado sesi�n");
            a.setHeaderText(null);
            a.setContentText("Cierra sesi�n si deseas registrar una nueva cuenta.");
            a.showAndWait();
            return;
        }
        if (coordinador != null) coordinador.mostrarRegistro();
    }

    private String safeCiudad() { return ciudadSeleccionada != null ? ciudadSeleccionada : "sin ciudad"; }

    private void onLogout() {
        Session.clear();
        this.usuario = null;
        if (lblUserName != null) { lblUserName.setText(""); lblUserName.setVisible(false); }
        if (btnIniciarSesion != null) { btnIniciarSesion.setVisible(true); btnIniciarSesion.setText("Iniciar sesi�n"); }
        if (btnRegistrarse != null) btnRegistrarse.setDisable(false);
        updateMenuPerfilState();
    }

    private void refreshSessionUI() {
        boolean logged = Session.isLoggedIn();
        if (logged) {
            var u = Session.getCurrent();
            String label = "";
            if (u != null) {
                if (u.getNombre() != null && !u.getNombre().isBlank()) label = u.getNombre();
                else if (u.getEmail() != null) {
                    String e = u.getEmail(); int at = e.indexOf('@'); label = at > 0 ? e.substring(0, at) : e;
                }
            }
            if (lblUserName != null) { lblUserName.setText(label); lblUserName.setVisible(true); }
            if (btnIniciarSesion != null) btnIniciarSesion.setVisible(false);
            if (btnRegistrarse  != null) btnRegistrarse.setDisable(true);
        } else {
            if (lblUserName != null) { lblUserName.setText(""); lblUserName.setVisible(false); }
            if (btnIniciarSesion != null) { btnIniciarSesion.setVisible(true); btnIniciarSesion.setText("Iniciar sesi�n"); }
            if (btnRegistrarse  != null) btnRegistrarse.setDisable(false);
        }
        updateMenuPerfilState();
    }

    private void updateMenuPerfilState() {
        boolean logged = Session.isLoggedIn();
        if (menuPerfil != null) {
            menuPerfil.setDisable(!logged);
            menuPerfil.setVisible(logged);
            menuPerfil.setManaged(logged);
        }
    }

    @FXML private void onPromoVerMas() { System.out.println("Promoci�n � Ver m�s (" + safeCiudad() + ")"); }
    @FXML private void onCard1(){ System.out.println("Card 1 � Ver m�s (" + safeCiudad() + ")"); }
    @FXML private void onCard2(){ System.out.println("Card 2 � Ver m�s (" + safeCiudad() + ")"); }
    @FXML private void onCard3(){ System.out.println("Card 3 � Ver m�s (" + safeCiudad() + ")"); }
    @FXML private void onCard4(){ System.out.println("Card 4 � Ver m�s (" + safeCiudad() + ")"); }

    private void onSeleccionarCiudad() {
        String ciudad = (cbCiudad != null) ? cbCiudad.getValue() : null;
        if (ciudad == null || ciudad.isBlank()) return;

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/sigmacine/ui/views/pagina_inicial.fxml"));
            Parent root = loader.load();
            ClienteController controller = loader.getController();
            controller.init(this.usuario, ciudad);

            Stage stage = (Stage) btnSeleccionarCiudad.getScene().getWindow();
            stage.setTitle("Sigma Cine - Cliente (" + ciudad + ")");
            Scene current = stage.getScene();
            double w = current != null ? current.getWidth() : 900;
            double h = current != null ? current.getHeight() : 600;
            stage.setScene(new Scene(root, w > 0 ? w : 900, h > 0 ? h : 600));
            stage.show();
            stage.setMaximized(true);
        } catch (Exception ex) {
            throw new RuntimeException("Error cargando pagina_inicial.fxml", ex);
        }
    }

    @FXML
    private void onBuscarTop() { doSearch(txtBuscar != null ? txtBuscar.getText() : ""); }

    private void doSearch(String texto) {
        if (texto == null) texto = "";
        try {
            DatabaseConfig db = new DatabaseConfig();
            var repo = new PeliculaRepositoryJdbc(db);
            var resultados = repo.buscarPorTitulo(texto);

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/sigmacine/ui/views/resultados_busqueda.fxml"));
            Parent root = loader.load();
            ResultadosBusquedaController controller = loader.getController();
            controller.setCoordinador(this.coordinador);
            controller.setUsuario(this.usuario);
            controller.setResultados(resultados, texto);

            Stage stage = (Stage) content.getScene().getWindow();
            Scene current = stage.getScene();
            double w = current != null ? current.getWidth() : 900;
            double h = current != null ? current.getHeight() : 600;
            stage.setScene(new Scene(root, w > 0 ? w : 900, h > 0 ? h : 600));
            stage.setTitle("Resultados de b�squeda");
            stage.setMaximized(true);
        } catch (Exception ex) {
            ex.printStackTrace();
            content.getChildren().setAll(new Label("Error cargando resultados: " + ex.getMessage()));
        }
    }

    @FXML
    private void onVerHistorial() {
        if (!Session.isLoggedIn()) {
            var a = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
            a.setTitle("Acceso denegado");
            a.setHeaderText(null);
            a.setContentText("Debes iniciar sesi�n para ver tu historial de compras.");
            a.showAndWait();
            return;
        }
        try {
            DatabaseConfig dbConfig = new DatabaseConfig();
            var usuarioRepo = new UsuarioRepositoryJdbc(dbConfig);
            var historialService = new VerHistorialService(usuarioRepo);

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/sigmacine/ui/views/VerCompras.fxml"));
            VerHistorialController historialController = new VerHistorialController(historialService);
            historialController.setClienteController(this);

            if (this.usuario != null) historialController.setUsuarioEmail(this.usuario.getEmail());

            loader.setController(historialController);
            Parent historialView = loader.load();
            content.getChildren().setAll(historialView);
        } catch (Exception ex) {
            ex.printStackTrace();
            content.getChildren().setAll(new Label("Error cargando Historial: " + ex.getMessage()));
        }
    }

    public void mostrarCartelera() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/sigmacine/ui/views/contenidoCartelera.fxml"));
            Parent carteleraView = loader.load();

            Object ctrlObj = loader.getController();
            if (ctrlObj instanceof ContenidoCarteleraController c) {
                c.setCoordinador(this.coordinador);
                c.setUsuario(this.usuario);
            } else {
                try {
                    var m = ctrlObj.getClass().getMethod("init", sigmacine.aplicacion.data.UsuarioDTO.class);
                    if (m != null) m.invoke(ctrlObj, this.usuario);
                } catch (NoSuchMethodException ignore) {}
                try {
                    var su = ctrlObj.getClass().getMethod("setUsuario", sigmacine.aplicacion.data.UsuarioDTO.class);
                    if (su != null) su.invoke(ctrlObj, this.usuario);
                } catch (NoSuchMethodException ignore) {}
                try {
                    var sc = ctrlObj.getClass().getMethod("setCoordinador", sigmacine.ui.controller.ControladorControlador.class);
                    if (sc != null) sc.invoke(ctrlObj, this.coordinador);
                } catch (NoSuchMethodException ignore) {}
            }

            Stage stage = (Stage) content.getScene().getWindow();
            Scene current = stage.getScene();
            double w = current != null ? current.getWidth() : 900;
            double h = current != null ? current.getHeight() : 600;
            stage.setScene(new Scene(carteleraView, w > 0 ? w : 900, h > 0 ? h : 600));
            stage.setTitle("Sigma Cine - Cartelera");
            stage.setMaximized(true);
        } catch (Exception e) {
            e.printStackTrace();
            content.getChildren().setAll(new Label("Error: No se pudo cargar la vista de cartelera."));
        }
    }

    private void updatePublicidadVisibility() {
        try {
            boolean hasContent = content != null && !content.getChildren().isEmpty();
            boolean show = !hasContent;
            if (imgPublicidad != null) { imgPublicidad.setVisible(show); imgPublicidad.setManaged(show); }
            if (promoPane     != null) { promoPane.setVisible(show);     promoPane.setManaged(show); }
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    public boolean isSameScene(Node n) { return content != null && n != null && content.getScene() == n.getScene(); }

    private void togglePromo(boolean show) {
        if (promoPane != null){ promoPane.setVisible(show); promoPane.setManaged(show); }
        if (imgPublicidad != null){ imgPublicidad.setVisible(show); imgPublicidad.setManaged(show); }
        if (lblPromo != null){ lblPromo.setVisible(show); lblPromo.setManaged(show); }
        if (btnPromoVerMas != null){ btnPromoVerMas.setVisible(show); btnPromoVerMas.setManaged(show); }
    }

    // Solo pruebas locales
    @SuppressWarnings("unused")
    private void mostrarAsientosAhora() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/sigmacine/ui/views/asientos.fxml"));
            Parent pane = loader.load();

            AsientosController ctrl = loader.getController();
            var ocup  = java.util.Set.of("B3","B4","C7","E2","F8");
            var acces = java.util.Set.of("E3","E4","E5","E6");
            ctrl.setFuncion("Los 4 Fant�sticos", "1:10 pm", ocup, acces);

            content.getChildren().setAll(pane);
        } catch (Exception e) { e.printStackTrace(); }
    }

    public void volverHomeContenido() { togglePromo(true); }

    /** Flujo Cartelera -> Asientos embebido en `content`. */
    public void mostrarAsientos(String titulo, String hora,
                                java.util.Set<String> ocupados,
                                java.util.Set<String> accesibles) {
        try {
            togglePromo(false);

            var url = getClass().getResource("/sigmacine/ui/views/asientos_contenido.fxml");
            if (url == null) url = getClass().getResource("/sigmacine/ui/views/asientos.fxml");

            FXMLLoader loader = new FXMLLoader(java.util.Objects.requireNonNull(url, "No se encontr� la vista de asientos"));
            Parent pane = loader.load();

            AsientosController ctrl = loader.getController();
            ctrl.setFuncion(titulo, hora, ocupados, accesibles);

            content.getChildren().setAll(pane);
        } catch (Exception e) {
            e.printStackTrace();
            content.getChildren().setAll(new Label("Error cargando Asientos: " + e.getMessage()));
        }
    }
}