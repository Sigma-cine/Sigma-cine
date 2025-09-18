import java.io.InputStream; 
import java.io.InputStreamReader; 
import java.io.IOException; // <--- AGREGAR ESTA IMPORTACIÓN 
import java.nio.charset.StandardCharsets; 
import java.sql.Connection; 
import java.sql.SQLException; 
import org.h2.tools.RunScript; 
public class ScriptLoader { 
    public static void runScripts(Connection conn) {
         try {
             // Ejecutar schema.sql desde resources
              runSqlFromClasspath(conn, "/schema.sql");
               // Ejecutar data.sql desde resources (si existe)
                runSqlFromClasspath(conn, "/data.sql"); 
                System.out.println("Scripts ejecutados correctamente"); 
            } catch (SQLException | IOException e) 
            {
                     // <--- MODIFICAR ESTA LÍNEA 
                     System.err.println("Error ejecutando scripts: " + e.getMessage()); e.printStackTrace();
                     } 
                    } 
                    private static void runSqlFromClasspath(Connection conn, String resourcePath) throws SQLException, IOException {
                         // <--- MODIFICAR ESTA LÍNEA
                          InputStream in = ScriptLoader.class.getResourceAsStream(resourcePath); 
                          if (in == null) { System.out.println("No se encontró " + resourcePath + " en resources (se omite).");
                           return; 
                        } 
                        try (InputStreamReader reader = new InputStreamReader(in, StandardCharsets.UTF_8))
                         { RunScript.execute(conn, reader); 
                            System.out.println("Ejecutado: " + resourcePath); 
                        } 
                    } 
                }