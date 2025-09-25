package sigmacine.infraestructura.persistencia.jdbc;

import sigmacine.infraestructura.configdatabase.DatabaseConfig;
import sigmacine.dominio.repository.UsuarioRepository;
import sigmacine.dominio.entity.Admin;
import sigmacine.dominio.entity.Cliente;
import sigmacine.dominio.entity.Usuario;
import sigmacine.dominio.valueobject.Email;
import java.lang.Long;
import java.lang.String;
import sigmacine.dominio.valueobject.PasswordHash;

import java.sql.*;
import java.util.Optional;

public class UsuarioRepositoryJdbc implements UsuarioRepository {

    private final DatabaseConfig db;

    public UsuarioRepositoryJdbc(DatabaseConfig db) {
        this.db = db;
    }

    @Override
    public Optional<Usuario> buscarPorEmail(Email email) {
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
                if (!rs.next()) return Optional.empty();
                return Optional.of(mapRowToDomain(rs));
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
            ps.setString(2, u.getRol());
            ps.setLong(3, u.getId());

            if (ps.executeUpdate() == 0) {
                throw new RuntimeException("USUARIO no existe (id=" + u.getId() + ")");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error actualizando USUARIO", e);
        }
    }

    public Long crearCliente(Email email, PasswordHash passwordHash, String nombre) {
        final String nextIdSql      = "SELECT COALESCE(MAX(ID),0)+1 AS NEXT_ID FROM USUARIO";
        final String insertUsuario  = "INSERT INTO USUARIO (ID, EMAIL, CONTRASENA, ROL) VALUES (?, ?, ?, 'CLIENTE')";
        final String insertCliente  = "INSERT INTO CLIENTE (ID, NOMBRE, FECHA_REGISTRO) VALUES (?, ?, CURRENT_DATE)";

        try (Connection con = db.getConnection()) {
            con.setAutoCommit(false);
            try {
                long id;
                try (PreparedStatement ps = con.prepareStatement(nextIdSql);
                     ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    id = rs.getLong("NEXT_ID");
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

    private Usuario mapRowToDomain(ResultSet rs) throws SQLException {
        Long id         = rs.getLong("ID");
        Email email     = new Email(rs.getString("EMAIL"));
        PasswordHash ph = new PasswordHash(rs.getString("CONTRASENA"));
        String rolBd    = rs.getString("ROL");
        String rol      = normalizarRol(rolBd);

        if (Usuario.ROL_ADMIN.equals(rol)) {
            String nombreAdmin = rs.getString("NOMBRE_ADMIN");
            return new Admin(id, email, ph, nombreAdmin);
        } else {
            String nombreCliente = rs.getString("NOMBRE_CLIENTE");
            Date f = rs.getDate("FECHA_REGISTRO");
            String fechaRegistro = (f != null) ? f.toLocalDate().toString() : null;
            return new Cliente(id, email, ph, nombreCliente, fechaRegistro);
        }
    }

    private String normalizarRol(String raw) {
        if (raw == null) return Usuario.ROL_CLIENTE;
        String v = raw.trim().toUpperCase();
        if (v.equals("ADMIN") || v.equals("ADMI")) return Usuario.ROL_ADMIN;
        if (v.equals("CLIENTE") || v.equals("USUARIO")) return Usuario.ROL_CLIENTE;
        return Usuario.ROL_CLIENTE;
    }
}
