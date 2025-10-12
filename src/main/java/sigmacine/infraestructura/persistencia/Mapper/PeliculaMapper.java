package sigmacine.infraestructura.persistencia.Mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import sigmacine.dominio.entity.Pelicula;

public class PeliculaMapper {

    public static Pelicula map(ResultSet rs) throws SQLException {       
         Pelicula p = new Pelicula(
            rs.getInt("ID"),
            rs.getString("TITULO"),
            rs.getString("GENERO"),
            rs.getString("CLASIFICACION"),
            (Integer) rs.getObject("DURACION"),
            rs.getString("DIRECTOR"),
            rs.getString("ESTADO")
        ); 
         String posterUrl = rs.getString("POSTER_URL"); // puede venir null
        if (posterUrl != null) {
            p.setPosterUrl(posterUrl);
        }

        String sinopsis = rs.getString("SINOPSIS");
        if (sinopsis != null) {
            p.setSinopsis(sinopsis);
        }

        // Reparto: guardamos la cadena tal cual (coma-separada)
        String reparto = rs.getString("REPARTO");
        if (reparto != null) {
            p.setReparto(reparto);
        }

        return p;
    }
    
}