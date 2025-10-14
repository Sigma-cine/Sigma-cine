package sigmacine.ui.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;

import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

public class AsientosController implements Initializable {

    @FXML private GridPane gridSala;
    @FXML private Label lblResumen;

    @FXML private Label lblTitulo;
    @FXML private Label lblHoraPill;
    @FXML private ImageView imgPoster;
    @FXML private Button btnContinuar;

    private int filas = 8;
    private int columnas = 12;

    private final Set<String> ocupados   = new HashSet<>();
    private final Set<String> accesibles = new HashSet<>();
    private final Set<String> seleccion  = new HashSet<>();
    private final Map<String, ToggleButton> seatByCode = new HashMap<>();

    private String titulo = "Película";
    private String hora   = "1:10 pm";
    private Image poster;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Demo si nadie setea función
        if (ocupados.isEmpty()) {
            for (int c = 3; c <= columnas; c += 2) ocupados.add("D" + c);
            for (int c = 2; c <= columnas; c += 3) ocupados.add("E" + c);
            for (int c = 1; c <= columnas; c += 4) ocupados.add("F" + c);
        }

        // Por defecto: accesibles en primera fila A con corrimiento +2 (A5..A8)
        if (accesibles.isEmpty()) {
            accesibles.addAll(Arrays.asList("A5","A6","A7","A8"));
        }

        if (lblTitulo != null)   lblTitulo.setText(titulo);
        if (lblHoraPill != null) lblHoraPill.setText(hora);
        if (imgPoster != null && poster != null) imgPoster.setImage(poster);

        poblarGrilla();
        actualizarResumen();
    }

    private void poblarGrilla() {
        gridSala.getChildren().clear();
        seatByCode.clear();
        seleccion.clear();

        for (int f = 0; f < filas; f++) {
            for (int c = 0; c < columnas; c++) {
                String code = code(f, c);

                ToggleButton seat = new ToggleButton();
                seat.getStyleClass().add("seat"); // definido en CSS como .toggle-button.seat
                seat.setUserData(code);
                seat.setFocusTraversable(false);
                seat.setTooltip(new Tooltip(code));

                // marca si es accesible; el estilo (glow) se aplica en setSeatState
                seat.getProperties().put("accessible", accesibles.contains(code));

                if (ocupados.contains(code)) {
                    setSeatState(seat, SeatState.UNAVAILABLE);
                    seat.setDisable(true);
                } else {
                    setSeatState(seat, SeatState.AVAILABLE);
                    seat.setOnAction(e -> {
                        if (seat.isSelected()) {
                            setSeatState(seat, SeatState.SELECTED); // rojo para todos
                            seleccion.add(code);
                        } else {
                            setSeatState(seat, SeatState.AVAILABLE);
                            seleccion.remove(code);
                        }
                        actualizarResumen();
                    });
                }

                seatByCode.put(code, seat);
                gridSala.add(seat, c, f);
            }
        }
    }

    private enum SeatState { AVAILABLE, SELECTED, UNAVAILABLE }

    // ► Accesible: glow SIEMPRE en AVAILABLE y SELECTED. Unavailable sin glow.
    private void setSeatState(ToggleButton b, SeatState st) {
        b.getStyleClass().removeAll("seat--available", "seat--selected", "seat--unavailable", "seat--accessible");
        boolean isAccessible = Boolean.TRUE.equals(b.getProperties().get("accessible"));

        switch (st) {
            case AVAILABLE -> {
                b.getStyleClass().add("seat--available");
                if (isAccessible) b.getStyleClass().add("seat--accessible"); // brillo SIEMPRE
            }
            case SELECTED -> {
                b.getStyleClass().add("seat--selected");     // rojo para TODOS
                if (isAccessible) b.getStyleClass().add("seat--accessible"); // brillo también seleccionado
            }
            case UNAVAILABLE -> b.getStyleClass().add("seat--unavailable");
        }
        b.setSelected(st == SeatState.SELECTED);
    }

    private String code(int filaIdx, int colIdx) {
        char fila = (char) ('A' + filaIdx);
        return fila + String.valueOf(colIdx + 1);
    }

    private void actualizarResumen() {
        int n = seleccion.size();
        if (lblResumen != null) {
            lblResumen.setText(n + (n == 1 ? " Silla seleccionada" : " Sillas seleccionadas"));
        }
        if (btnContinuar != null) btnContinuar.setDisable(n == 0);
    }

    // =======================
    // API que usan tus controladores
    // =======================

    /**
     * Reubica los "accesibles" recibidos a la PRIMERA FILA y +2 columnas (E3..E6 -> A5..A8).
     */
    public void setFuncion(String titulo,
                           String hora,
                           java.util.Set<String> ocupados,
                           java.util.Set<String> accesibles) {
        if (titulo != null) this.titulo = titulo;
        if (hora   != null) this.hora   = hora;

        this.ocupados.clear();
        if (ocupados != null) this.ocupados.addAll(ocupados);

        this.accesibles.clear();
        if (accesibles != null && !accesibles.isEmpty()) {
            this.accesibles.addAll(shiftAccesiblesToFirstRowPlus2(accesibles));
        } else {
            this.accesibles.addAll(Arrays.asList("A5","A6","A7","A8"));
        }

        if (lblTitulo != null)   lblTitulo.setText(this.titulo);
        if (lblHoraPill != null) lblHoraPill.setText(this.hora);
        if (gridSala != null) { poblarGrilla(); actualizarResumen(); }
    }

    public void setPoster(Image poster) {
        this.poster = poster;
        if (imgPoster != null && poster != null) imgPoster.setImage(poster);
    }

    public void setFuncionConPoster(String titulo, String hora, Collection<String> ocupados, Image poster) {
        setFuncion(titulo, hora,
                ocupados == null ? Collections.emptySet() : new HashSet<>(ocupados),
                null);
        setPoster(poster);
    }

    public void configurarTamanoSala(int filas, int columnas) {
        this.filas = filas; this.columnas = columnas;
        if (gridSala != null) { poblarGrilla(); actualizarResumen(); }
    }

    public List<String> getSeleccionados() {
        return seleccion.stream().sorted().collect(Collectors.toList());
    }

    @FXML
    private void onContinuar() {
        System.out.println("[Asientos] seleccionados => " + getSeleccionados());
    }

    // ----- helpers -----
    private Set<String> shiftAccesiblesToFirstRowPlus2(Set<String> entrada) {
        Set<String> out = new HashSet<>();
        for (String code : entrada) {
            if (code == null || code.isBlank()) continue;
            try {
                int col = Integer.parseInt(code.substring(1));
                int nueva = Math.min(Math.max(col + 2, 1), columnas);
                out.add("A" + nueva);
            } catch (NumberFormatException ignore) {}
        }
        return out;
    }
}
