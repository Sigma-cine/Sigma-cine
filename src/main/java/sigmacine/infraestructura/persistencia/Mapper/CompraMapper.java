package sigmacine.infraestructura.persistencia.Mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import sigmacine.dominio.entity.Compra;
import sigmacine.dominio.entity.Usuario;

public final class CompraMapper {

    private CompraMapper() {}
    public static Compra map(ResultSet rs, Usuario cliente) throws SQLException {
        Long id = rs.getLong("COMPRA_ID"); 
        return new Compra(id, cliente);
    }
}
