package sigmacine.ui.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import sigmacine.dominio.entity.Pelicula;

import java.util.List;

public class BuscarPeliculaController {

    @FXML private TextField txtTitulo;
    @FXML private TableView<Pelicula> tablaPeliculas;
    @FXML private TableColumn<Pelicula, Long> colId;
    @FXML private TableColumn<Pelicula, String> colTitulo, colGenero, colClasificacion, colDireccion, colReparto, colTrailer, colSinopsis, colEstado;
    @FXML private TableColumn<Pelicula, Integer> colDuracion;

    private ControladorControlador coordinador;
    private java.util.function.Function<String, List<Pelicula>> buscador; // inyecta desde afuera

    public void setCoordinador(ControladorControlador c) { this.coordinador = c; }
    /** Permite inyectar el servicio de búsqueda que ya tengas (por título) */
    public void setBuscador(java.util.function.Function<String,List<Pelicula>> buscador) { this.buscador = buscador; }

    @FXML
    private void initialize() {
        if (colId != null) colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        if (colTitulo != null) colTitulo.setCellValueFactory(new PropertyValueFactory<>("titulo"));
        if (colGenero != null) colGenero.setCellValueFactory(new PropertyValueFactory<>("genero"));
        if (colClasificacion != null) colClasificacion.setCellValueFactory(new PropertyValueFactory<>("clasificacion"));
        if (colDuracion != null) colDuracion.setCellValueFactory(new PropertyValueFactory<>("duracion"));
        if (colDireccion != null) colDireccion.setCellValueFactory(new PropertyValueFactory<>("direccion"));
        if (colReparto != null) colReparto.setCellValueFactory(new PropertyValueFactory<>("reparto"));
        if (colTrailer != null) colTrailer.setCellValueFactory(new PropertyValueFactory<>("trailer"));
        if (colSinopsis != null) colSinopsis.setCellValueFactory(new PropertyValueFactory<>("sinopsis"));
        if (colEstado != null) colEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));
    }

    @FXML
    private void onBuscar() {
        if (buscador == null) return;
        String texto = txtTitulo.getText() == null ? "" : txtTitulo.getText().trim();
        List<Pelicula> lista = buscador.apply(texto);
        tablaPeliculas.getItems().setAll(lista);
    }

 /*   @FXML
    private void onVolver() {
        if (coordinador != null) coordinador.mostrarCartelera();
    }*/
}
