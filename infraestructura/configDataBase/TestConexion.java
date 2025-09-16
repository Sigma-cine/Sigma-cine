import java.sql.Connection;
import java.sql.SQLException;

public class TestConexion {

    public static void main(String[] args) {
        DatabaseConfig dbConfig = new DatabaseConfig();
        Connection connection = null;

        try {
            connection = dbConfig.GetConexionDBH2();
            if (connection != null) {
                System.out.println("¡Conexión exitosa! La base de datos está accesible.");
            } else {
                System.out.println("Fallo al conectar. Revisa la URL, el usuario y la contraseña.");
            }
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                    System.out.println("Conexión cerrada.");
                } catch (SQLException e) {
                    System.err.println("Error al cerrar la conexión: " + e.getMessage());
                }
            }
        }
    }
}