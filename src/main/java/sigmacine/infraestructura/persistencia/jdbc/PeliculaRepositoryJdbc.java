package sigmacine.infraestructura.persistencia.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import sigmacine.dominio.entity.Pelicula;
import sigmacine.dominio.repository.PeliculaRepository;
import sigmacine.infraestructura.configDataBase.DatabaseConfig;
import sigmacine.infraestructura.persistencia.Mapper.PeliculaMapper;

/*Se utilizara la misma estructura de codigo en las funciones buscarPorTitulo, buscarPorGenero y buscarTodas,
 por que solo se debe hacer una busqueda como tal y teniendo la coneccion a la base de datos H2, la unica diferencia es 
 la consulta sql.
 */

public class PeliculaRepositoryJdbc implements PeliculaRepository {
 
     private final DatabaseConfig db;

    public PeliculaRepositoryJdbc(DatabaseConfig db) {
        this.db = db;
    }
     

    @Override
    public List<Pelicula> buscarPorTitulo(String q) {
        String sql = "SELECT ID,TITULO,GENERO,CLASIFICACION,DURACION,DIRECTOR,ESTADO,POSTER_URL " +
                     "FROM PELICULA WHERE UPPER(TITULO) LIKE UPPER(?)";
        try (Connection cn = db.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, "%"+q+"%");
            try (ResultSet rs = ps.executeQuery()) {
                List<Pelicula> out = new ArrayList<>();
                while (rs.next()){
                     out.add(PeliculaMapper.map(rs));
                    }
                return out;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error en la buscando por título", e);
        }
    }

    @Override
    public List<Pelicula> buscarPorGenero(String genero){
        String sql = "SELECT ID,TITULO,GENERO,CLASIFICACION,DURACION,DIRECTOR,ESTADO,POSTER_URL " +
                     "FROM PELICULA WHERE UPPER(GENERO) LIKE UPPER(?)";
        try (Connection cn = db.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, "%"+genero+"%");
            try (ResultSet rs = ps.executeQuery()) {
                List<Pelicula> out = new ArrayList<>();
                while (rs.next()){ 
                    out.add(PeliculaMapper.map(rs));
                }
                return out;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error en la busqueda por genero", e);
        }

     }
     
     @Override
    public List<Pelicula> buscarTodas(){
         String sql = "SELECT ID,TITULO,GENERO,CLASIFICACION,DURACION,DIRECTOR,ESTADO,POSTER_URL " +
                     "FROM PELICULA";
        try (Connection cn = db.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            List<Pelicula> out = new ArrayList<>();
            while (rs.next()){ 
                out.add(PeliculaMapper.map(rs));
            }
            return out;

        } catch (SQLException e) {
            throw new RuntimeException("Error en el listado de las peliculas", e);
        }
     }
}
