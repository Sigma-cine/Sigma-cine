import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
public class PeliculaRepositoryjdbc {

    private final DatabaseConfig dbConfig = new DatabaseConfig();

    public void mostrarTodasLasPeliculas() {
        String sql = "SELECT ID, TITULO, DIRECTOR FROM PELICULA";

        try (Connection conn = dbConfig.GetConexionDBH2();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                long id = rs.getLong("ID");
                String titulo = rs.getString("TITULO");
                String director = rs.getString("DIRECTOR");
                System.out.println("ID: " + id + ", Titulo: " + titulo + ", Director: " + director);
            }
        } catch (SQLException e) {
            System.err.println("Error al ejecutar la consulta: " + e.getMessage());
        }
    }
}