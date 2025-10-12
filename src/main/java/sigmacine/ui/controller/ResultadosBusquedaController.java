package sigmacine.ui.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.geometry.Pos;
import sigmacine.dominio.entity.Pelicula;
import java.util.List;

public class ResultadosBusquedaController {
    @FXML private javafx.scene.control.Button btnVolver;
    @FXML private Label lblTituloResultados;
    @FXML private Label lblTextoBuscado;
    @FXML private HBox panelPeliculas;

    private List<Pelicula> peliculas;
    private String textoBuscado;

    public void setResultados(List<Pelicula> peliculas, String textoBuscado) {
        this.peliculas = peliculas;
        this.textoBuscado = textoBuscado;
        mostrarResultados();

        if (btnVolver != null) {
            btnVolver.setOnAction(e -> volverAInicio());
        }
    }

    private void volverAInicio() {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/sigmacine/ui/views/pagina_inicial.fxml"));
            javafx.scene.Parent root = loader.load();
            javafx.stage.Stage stage = (javafx.stage.Stage) btnVolver.getScene().getWindow();
            stage.setScene(new javafx.scene.Scene(root));
            stage.setTitle("Sigma Cine");
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
        if (peliculas == null) return;
        for (Pelicula p : peliculas) {
            VBox tarjeta = new VBox(8);
            tarjeta.setAlignment(Pos.CENTER);
            tarjeta.setPrefWidth(320);
            tarjeta.setPrefHeight(420);
            tarjeta.setMinWidth(320);
            tarjeta.setMaxWidth(320);
            tarjeta.setMinHeight(420);
            tarjeta.setMaxHeight(420);
            tarjeta.setStyle("-fx-background-color: #222; -fx-background-radius: 20; -fx-padding: 20; -fx-effect: dropshadow(gaussian, #000, 8, 0.2, 0, 2);");
            ImageView poster = new ImageView();
            poster.setFitHeight(180);
            poster.setFitWidth(120);
            if (p.getPosterUrl() != null && !p.getPosterUrl().isEmpty()) {
                try {
                    poster.setImage(new Image(p.getPosterUrl(), true));
                } catch (Exception e) {
                    poster.setImage(null);
                }
            }
            Label titulo = new Label(p.getTitulo());
            titulo.setStyle("-fx-font-size: 22px; -fx-text-fill: #fff; -fx-font-weight: bold;");
            Label genero = new Label("Género: " + p.getGenero());
            genero.setStyle("-fx-text-fill: #ccc;");
            Label clasificacion = new Label("Clasificación: " + p.getClasificacion());
            clasificacion.setStyle("-fx-text-fill: #ccc;");
            Label duracion = new Label("Duración: " + p.getDuracion() + " min");
            duracion.setStyle("-fx-text-fill: #ccc;");
            Label director = new Label("Director: " + p.getDirector());
            director.setStyle("-fx-text-fill: #ccc;");
            Label reparto = new Label("Reparto: " + (p.getReparto() != null ? String.join(", ", p.getReparto()) : ""));
            reparto.setStyle("-fx-text-fill: #ccc;");
            reparto.setWrapText(true);
            Label sinopsis = new Label("Sinopsis: " + p.getSinopsis());
            sinopsis.setStyle("-fx-text-fill: #eee; -fx-font-size: 14px;");
            sinopsis.setWrapText(true);
            javafx.scene.control.Button btnDetalle = new javafx.scene.control.Button("Ver detalle película");
            btnDetalle.setStyle("-fx-background-color: #993726; -fx-text-fill: #fff; -fx-font-weight: bold; -fx-background-radius: 30;");
            btnDetalle.setOnAction(e -> mostrarDetallePelicula(p));
            VBox.setMargin(btnDetalle, new javafx.geometry.Insets(10, 0, 0, 0));
            tarjeta.getChildren().addAll(poster, titulo, genero, clasificacion, duracion, director, reparto, sinopsis, btnDetalle);
            panelPeliculas.getChildren().add(tarjeta);
        }
    }
private void mostrarDetallePelicula(Pelicula p) {
    try {
        var url = getClass().getResource("/sigmacine/ui/views/contenidoCartelera.fxml");
        if (url == null) throw new IllegalStateException("No se encontró detalle_pelicula.fxml");

        javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(url);
        javafx.scene.Parent rootDetalle = loader.load();

        DetallePeliculaController ctrl = loader.getController();
        ctrl.setPelicula(p);

        javafx.stage.Stage stage = (javafx.stage.Stage) btnVolver.getScene().getWindow();
        javafx.scene.Scene scene = new javafx.scene.Scene(rootDetalle);
        // opcional: scene.getStylesheets().add(...);
        stage.setScene(scene);
        stage.setTitle(p.getTitulo() != null ? p.getTitulo() : "Detalle de Película");
        // stage.sizeToScene(); // úsalo solo si quieres que ajuste el tamaño a la nueva vista
    } catch (Exception ex) {
        ex.printStackTrace();
    }
}
}
