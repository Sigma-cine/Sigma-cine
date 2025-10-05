package sigmacine.infraestructura.persistencia.jdbc;

import sigmacine.infraestructura.configDataBase.DatabaseConfig;
import sigmacine.dominio.repository.UsuarioRepository;
import sigmacine.dominio.entity.Compra;
import sigmacine.dominio.entity.Usuario;
import sigmacine.dominio.valueobject.Email;
import sigmacine.dominio.valueobject.PasswordHash;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.sql.Date;

public class UsuarioRepositoryJdbc implements UsuarioRepository {

    private final DatabaseConfig db;

    public UsuarioRepositoryJdbc(DatabaseConfig db) {
        this.db = db;
    }

    @Override
    public Usuario buscarPorEmail(Email email) {
        final String sql = """
            SELECT
                U.ID,
                U.EMAIL,
                U.CONTRASENA,
                U.ROL,
                A.NOMBRE  AS NOMBRE_ADMIN,
                C.NOMBRE  AS NOMBRE_CLIENTE,
                C.FECHA_REGISTRO
            FROM USUARIO U
            LEFT JOIN ADMIN   A ON A.ID = U.ID
            LEFT JOIN CLIENTE C ON C.ID = U.ID
            WHERE U.EMAIL = ?
            FETCH FIRST 1 ROWS ONLY
        """;

        try (Connection con = db.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, email.value());

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                return mapUsuario(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error consultando USUARIO por email", e);
        }
    }

    @Override
    public void guardar(Usuario u) {
        final String sql = """
            UPDATE USUARIO
               SET CONTRASENA = ?,
                   ROL        = ?
             WHERE ID = ?
        """;
        try (Connection con = db.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, u.getPasswordHash().value());
            ps.setString(2, u.getRol().name());
            ps.setLong(3, u.getId());

            if (ps.executeUpdate() == 0) {
                throw new RuntimeException("USUARIO no existe (id=" + u.getId() + ")");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error actualizando USUARIO", e);
        }
    }

    @Override
    public int crearCliente(Email email, PasswordHash passwordHash, String nombre) {
        final String nextIdSql     = "SELECT COALESCE(MAX(ID),0)+1 AS NEXT_ID FROM USUARIO";
        final String insertUsuario = "INSERT INTO USUARIO (ID, EMAIL, CONTRASENA, ROL) VALUES (?, ?, ?, 'CLIENTE')";
        final String insertCliente = "INSERT INTO CLIENTE (ID, NOMBRE, FECHA_REGISTRO) VALUES (?, ?, CURRENT_DATE)";

        try (Connection con = db.getConnection()) {
            con.setAutoCommit(false);
            try {
                int id;
                try (PreparedStatement ps = con.prepareStatement(nextIdSql);
                     ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    id = rs.getInt("NEXT_ID");
                }

                try (PreparedStatement psU = con.prepareStatement(insertUsuario);
                     PreparedStatement psC = con.prepareStatement(insertCliente)) {

                    psU.setLong(1, id);
                    psU.setString(2, email.value());
                    psU.setString(3, passwordHash.value());
                    psU.executeUpdate();

                    psC.setLong(1, id);
                    psC.setString(2, nombre);
                    psC.executeUpdate();
                }

                con.commit();
                return id;
            } catch (SQLException ex) {
                con.rollback();
                throw ex;
            } finally {
                con.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error registrando cliente", e);
        }
    }

    private Usuario mapUsuario(ResultSet rs) throws SQLException {
        int id         = rs.getInt("ID");
        Email email     = new Email(rs.getString("EMAIL"));
        PasswordHash ph = new PasswordHash(rs.getString("CONTRASENA"));

        Usuario.Rol rol = normalizarRol(rs.getString("ROL"));

        if (rol == Usuario.Rol.ADMIN) {
            String nombreAdmin = rs.getString("NOMBRE_ADMIN");// ADMIN no tiene fechaRegistro/SigmaCard
            return Usuario.crearAdmin(id, email, ph, nombreAdmin);
        } else {
            String nombreCliente = rs.getString("NOMBRE_CLIENTE");
            Date f = rs.getDate("FECHA_REGISTRO");    // LocalDate si viene de DB, si no, null
            java.time.LocalDate fecha = (f != null) ? f.toLocalDate() : null;
            return Usuario.crearCliente(id, email, ph, nombreCliente, fecha);
        }
    }

    private Usuario.Rol normalizarRol(String raw) {
        if (raw == null) return Usuario.Rol.CLIENTE;
        String v = raw.trim().toUpperCase();
        if (v.equals("ADMIN") || v.equals("ADMI")) return Usuario.Rol.ADMIN;
        return Usuario.Rol.CLIENTE;
    }

    @Override
    public Usuario buscarPorId(int id) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'buscarPorId'");
    }

    @Override
    public List<Compra> verHistorial(String emailPlano) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'verHistorial'");
    }
}
