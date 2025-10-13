package sigmacine.ui.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.geometry.Pos;
import sigmacine.dominio.entity.Pelicula;
import java.util.List;

public class ResultadosBusquedaController {
    @FXML private javafx.scene.control.Button btnVolver;
    @FXML private Label lblTituloResultados;
    @FXML private Label lblTextoBuscado;
    @FXML private VBox panelPeliculas;

    private List<Pelicula> peliculas;
    private String textoBuscado;
    private sigmacine.ui.controller.ControladorControlador coordinador;
    private sigmacine.aplicacion.data.UsuarioDTO usuario;

    public void setCoordinador(sigmacine.ui.controller.ControladorControlador coordinador) {
        this.coordinador = coordinador;
    }

    public void setUsuario(sigmacine.aplicacion.data.UsuarioDTO usuario) {
        this.usuario = usuario;
    }

    public void setResultados(List<Pelicula> peliculas, String textoBuscado) {
        this.peliculas = peliculas;
        this.textoBuscado = textoBuscado;
        System.out.println("[DEBUG] setResultados called: peliculas=" + (peliculas == null ? 0 : peliculas.size()) + " texto='" + textoBuscado + "'");
        mostrarResultados();

        if (btnVolver != null) {
            btnVolver.setOnAction(e -> volverAInicio());
        }
    }

    private void volverAInicio() {
        try {
            // If coordinator + usuario are available, delegate so the same session is preserved
            if (coordinador != null && usuario != null) {
                coordinador.mostrarHome(usuario);
                return;
            }

            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/sigmacine/ui/views/cliente_home.fxml"));
            javafx.scene.Parent root = loader.load();
            javafx.stage.Stage stage = (javafx.stage.Stage) btnVolver.getScene().getWindow();
            // preserve current window size when switching
            javafx.scene.Scene current = stage.getScene();
            double w = current != null ? current.getWidth() : 900;
            double h = current != null ? current.getHeight() : 600;
            stage.setScene(new javafx.scene.Scene(root, w > 0 ? w : 900, h > 0 ? h : 600));
            stage.setTitle("Sigma Cine");
            stage.setMaximized(true);
        } catch (Exception ex) {
            System.err.println("Error al volver a inicio: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void mostrarResultados() {
        if (lblTituloResultados != null) lblTituloResultados.setText("Resultados de búsqueda");
        if (lblTextoBuscado != null) lblTextoBuscado.setText(textoBuscado != null ? textoBuscado : "");
        if (panelPeliculas == null) return;
        panelPeliculas.getChildren().clear();
        if (peliculas == null || peliculas.isEmpty()) {
            // Mostrar mensaje amigable cuando no hay coincidencias
            Label msg = new Label("No hay coincidencias");
            msg.setStyle("-fx-text-fill: #ddd; -fx-font-size: 18px; -fx-font-weight: bold;");
            // Centrar el mensaje dentro del panel
            panelPeliculas.setAlignment(Pos.CENTER);
            panelPeliculas.getChildren().add(msg);
            return;
        }
        for (Pelicula p : peliculas) {
            VBox tarjeta = new VBox(8);
            tarjeta.setAlignment(Pos.CENTER);
            // let the tarjeta fill the available width; keep a fixed height for consistent cards
            tarjeta.setPrefHeight(420);
            tarjeta.prefWidthProperty().bind(panelPeliculas.widthProperty().subtract(40)); // account for padding
            tarjeta.setStyle("-fx-background-color: #222; -fx-background-radius: 20; -fx-padding: 20; -fx-effect: dropshadow(gaussian, #000, 8, 0.2, 0, 2);");
            ImageView poster = new ImageView();
            poster.setPreserveRatio(true);
            // we will bind poster fitHeight later to match the info column height

            javafx.scene.control.Label posterPlaceholder = new javafx.scene.control.Label("No image");
            posterPlaceholder.setStyle("-fx-text-fill: #999; -fx-font-size: 12px;");
            posterPlaceholder.setWrapText(true);
            javafx.scene.layout.StackPane posterPane = new javafx.scene.layout.StackPane();
            posterPane.getChildren().addAll(poster, posterPlaceholder);
            javafx.scene.layout.StackPane.setAlignment(posterPlaceholder, Pos.CENTER);
            javafx.scene.layout.StackPane.setAlignment(poster, Pos.CENTER);
            poster.visibleProperty().bind(poster.imageProperty().isNotNull());
            posterPlaceholder.visibleProperty().bind(poster.imageProperty().isNull());

            // Try load poster (remote URL or classpath resource under /Images)
            try {
                String posterRef = p.getPosterUrl();
                if (posterRef != null && !posterRef.isBlank()) {
                    String lower = posterRef.toLowerCase();
                    if (lower.startsWith("http://") || lower.startsWith("https://") || lower.startsWith("file:/")) {
                        poster.setImage(new Image(posterRef, true));
                    } else {
                        var res = getClass().getResource("/Images/" + posterRef);
                        if (res != null) poster.setImage(new Image(res.toExternalForm(), false));
                    }
                }
            } catch (Exception ex) {
                // leave poster null (placeholder will show)
            }

            // Info column (center) - reserve 1/3 width
            VBox infoCol = new VBox(6);
            infoCol.setAlignment(Pos.TOP_LEFT);
            infoCol.prefWidthProperty().bind(tarjeta.widthProperty().multiply(1.0/3.0));
            infoCol.setMaxWidth(Double.MAX_VALUE);
            javafx.scene.layout.HBox.setHgrow(infoCol, javafx.scene.layout.Priority.ALWAYS);

            Label titulo = new Label(p.getTitulo() != null ? p.getTitulo() : "Título desconocido");
            titulo.setStyle("-fx-font-size: 22px; -fx-text-fill: #fff; -fx-font-weight: bold;");
            titulo.setWrapText(true);
            Label genero = new Label("Género: " + (p.getGenero() != null ? p.getGenero() : "-"));
            genero.setStyle("-fx-text-fill: #ccc; -fx-font-size: 12px;");
            Label clasificacion = new Label("Clasificación: " + (p.getClasificacion() != null ? p.getClasificacion() : "-"));
            clasificacion.setStyle("-fx-text-fill: #ccc; -fx-font-size: 12px;");
            Label duracion = new Label("Duración: " + (p.getDuracion() > 0 ? p.getDuracion() + " min" : "-"));
            duracion.setStyle("-fx-text-fill: #ccc; -fx-font-size: 12px;");
            Label director = new Label("Director: " + (p.getDirector() != null ? p.getDirector() : "-"));
            director.setStyle("-fx-text-fill: #ccc; -fx-font-size: 12px;");
            String repartoText = (p.getReparto() != null && !p.getReparto().isBlank()) ? p.getReparto() : "No disponible";
            Label reparto = new Label("Reparto: " + repartoText);
            reparto.setStyle("-fx-text-fill: #ccc; -fx-font-size: 12px;");
            reparto.setWrapText(true);
            String sinopsisText = p.getSinopsis() != null ? p.getSinopsis() : "No disponible";
            Label sinopsis = new Label("Sinopsis: " + sinopsisText);
            // use same size/color as other info labels
            sinopsis.setStyle("-fx-text-fill: #ccc; -fx-font-size: 12px;");
            sinopsis.setWrapText(true);

            infoCol.getChildren().addAll(titulo, genero, clasificacion, duracion, director, reparto, sinopsis);

            // Ensure wrapping respects the info column width
            titulo.maxWidthProperty().bind(infoCol.widthProperty().subtract(10));
            titulo.setMaxHeight(javafx.scene.layout.Region.USE_PREF_SIZE);
            reparto.maxWidthProperty().bind(infoCol.widthProperty().subtract(10));
            sinopsis.maxWidthProperty().bind(infoCol.widthProperty().subtract(10));

            // Make posterPane follow a portion of the tarjeta height
            posterPane.prefHeightProperty().bind(tarjeta.heightProperty().multiply(0.9));

            // Bind poster fitHeight to the posterPane so the image scales properly
            poster.fitHeightProperty().bind(posterPane.heightProperty().multiply(0.95));
            poster.fitWidthProperty().bind(posterPane.widthProperty().multiply(0.9));

            // Button column (right) - reserve 1/3 width
            javafx.scene.layout.StackPane btnPane = new javafx.scene.layout.StackPane();
            btnPane.prefWidthProperty().bind(tarjeta.widthProperty().multiply(1.0/3.0));
            btnPane.setMaxWidth(Double.MAX_VALUE);
            btnPane.setAlignment(Pos.CENTER);
            javafx.scene.layout.HBox.setHgrow(btnPane, javafx.scene.layout.Priority.ALWAYS);

            javafx.scene.control.Button btnDetalle = new javafx.scene.control.Button("Ver detalle película");
            btnDetalle.setStyle("-fx-background-color: #993726; -fx-text-fill: #fff; -fx-font-weight: bold; -fx-background-radius: 30;");
            btnDetalle.setOnAction(e -> mostrarDetallePelicula(p));
            btnPane.getChildren().add(btnDetalle);

            // Compose row and add to tarjeta
            javafx.scene.layout.HBox fila = new javafx.scene.layout.HBox(12);
            fila.setAlignment(Pos.TOP_LEFT);
            fila.setStyle("-fx-padding: 6 0 6 0;");
            fila.getChildren().addAll(posterPane, infoCol, btnPane);

            tarjeta.getChildren().addAll(fila);
            panelPeliculas.getChildren().add(tarjeta);
        }
        // Simple layout pass
        javafx.application.Platform.runLater(() -> {
            try {
                panelPeliculas.applyCss();
                panelPeliculas.layout();
            } catch (Exception ignored) {}
        });
    }
private void mostrarDetallePelicula(Pelicula p) {
    try {
        var url = getClass().getResource("/sigmacine/ui/views/contenidoCartelera.fxml");
        if (url == null) throw new IllegalStateException("No se encontró detalle_pelicula.fxml");

        javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(url);
        javafx.scene.Parent rootDetalle = loader.load();

    DetallePeliculaController ctrl = loader.getController();
    // pass current results and search text so detail can return to them
    ctrl.setBackResults(this.peliculas, this.textoBuscado);
    ctrl.setPelicula(p);

        javafx.stage.Stage stage = (javafx.stage.Stage) btnVolver.getScene().getWindow();
        // Preserve current stage size when showing details
        javafx.scene.Scene current = stage.getScene();
        double w = current != null ? current.getWidth() : 900;
        double h = current != null ? current.getHeight() : 600;
        javafx.scene.Scene scene = new javafx.scene.Scene(rootDetalle, w > 0 ? w : 900, h > 0 ? h : 600);
        // opcional: scene.getStylesheets().add(...);
        stage.setScene(scene);
        stage.setTitle(p.getTitulo() != null ? p.getTitulo() : "Detalle de Película");
        // stage.sizeToScene(); // úsalo solo si quieres que ajuste el tamaño a la nueva vista
    } catch (Exception ex) {
        ex.printStackTrace();
    }
}
}
