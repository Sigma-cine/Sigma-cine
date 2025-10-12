package sigmacine.infraestructura.persistencia.Mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import sigmacine.dominio.entity.Compra;
import sigmacine.dominio.entity.Usuario;

public final class CompraMapper {

    private CompraMapper() {}
    public static Compra map(ResultSet rs, Usuario cliente) throws SQLException {
        Long id = rs.getLong("COMPRA_ID"); 
        Compra c = new Compra(id, cliente);
        try {
            // Mapeo de campos adicionales si vienen en el ResultSet
            java.math.BigDecimal totalBd = rs.getBigDecimal("COMPRA_TOTAL");
            java.sql.Date fechaSql = rs.getDate("COMPRA_FECHA");
            if (fechaSql != null) c.setFecha(fechaSql.toLocalDate());
            if (totalBd != null) c.setTotalDecimal(totalBd.doubleValue());
        } catch (SQLException ex) {
            // Si no vienen las columnas, devolvemos la compra con datos mínimos
        }
        return c;
    }
}
