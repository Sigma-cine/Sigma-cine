import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


public class DatabaseConfig {

public static final String Username = "sa";

public static final String Password = "";

public static final String URL = "jdbc:h2:file:~/sigmacine/db/cine_db;AUTO_SERVER=TRUE";

public static final String Drive_DB = "org.h2.Driver";


static {
        try {
            Class.forName(Drive_DB);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("No se encontró el driver de H2", e);
        }
    }
 

public Connection GetConexionDBH2(){
    
        Connection connection = null;
        try {
            Class.forName(Drive_DB);
            connection = DriverManager.getConnection(URL, Username, Password);
            System.out.println("Conexion exitosa!");
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
            System.err.println("Error en la conexion: " + e.getMessage());
        }
        return connection;
}
}