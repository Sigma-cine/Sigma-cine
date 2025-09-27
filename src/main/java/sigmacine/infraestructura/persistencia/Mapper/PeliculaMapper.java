package sigmacine.infraestructura.persistencia.Mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import sigmacine.dominio.entity.Pelicula;

public class PeliculaMapper {

    public static Pelicula map(ResultSet rs) throws SQLException {       
         return new Pelicula(
            rs.getInt("ID"),
            rs.getString("TITULO"),
            rs.getString("GENERO"),
            rs.getString("CLASIFICACION"),
            (Integer) rs.getObject("DURACION"),
            rs.getString("DIRECTOR"),
            rs.getString("ESTADO")
        ); 
         //Se omite reparto, trailer y  sinapsis 
    }
    
}