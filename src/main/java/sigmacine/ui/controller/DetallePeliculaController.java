package sigmacine.ui.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import sigmacine.dominio.entity.Pelicula;

import java.util.List;

public class DetallePeliculaController {

    @FXML private ImageView imgPoster;
    @FXML private TextArea txtSinopsis;
    @FXML private Label lblSinopsisTitulo;
    @FXML private VBox panelFunciones; // opcional si generas horarios por código
    @FXML private Button btnComprar;   // opcional

    @FXML private Label lblGenero, lblClasificacion, lblDuracion, lblDirector, lblReparto;

    private Pelicula pelicula;

    @FXML
    private void initialize() {
        if (btnComprar != null) {
            btnComprar.setOnAction(e -> {
                // TODO: navegación a flujo de compra con 'pelicula'
            });
        }
    }

    public void setPelicula(Pelicula p) {
        this.pelicula = p;

        // Poster
        String url = safe(p.getPosterUrl());
        if (!url.isEmpty()) {
            try { imgPoster.setImage(new Image(url, true)); } catch (Exception ignored) {}
        } else {
            imgPoster.setImage(null);
        }

        // Encabezado
        lblSinopsisTitulo.setText("SINOPSIS — " + safe(p.getTitulo(), "N/D"));

        // Ficha técnica (lo que mostraba el Alert)
        lblGenero.setText(safe(p.getGenero(), "N/D"));
        lblClasificacion.setText(safe(p.getClasificacion(), "N/D"));
        int dur = p.getDuracion();
        lblDuracion.setText(dur > 0 ? dur + " min" : "N/D");  // usa >0 si quieres mostrar N/D cuando venga 0

        lblDirector.setText(safe(p.getDirector(), "N/D"));
        lblReparto.setText(safe(join(p.getReparto()), "")); // puede ser largo

        // Sinopsis
        txtSinopsis.setText(safe(p.getSinopsis()));
    }

    /* ===== helpers ===== */

    private static String join(List<String> items) {
        return (items == null || items.isEmpty()) ? "" : String.join(", ", items);
    }

    private static String safe(String s) {
        return (s == null || s.trim().equalsIgnoreCase("null")) ? "" : s.trim();
    }

    private static String safe(String s, String alt) {
        String t = safe(s);
        return t.isEmpty() ? alt : t;
    }
}
