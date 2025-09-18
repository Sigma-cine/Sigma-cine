package sigmacine.dominio.entity;

import java.util.ArrayList;
import java.util.List;
import java.math.BigDecimal;
import sigmacine.dominio.entity.SigmaCard;
import sigmacine.dominio.valueobject.Email;
import sigmacine.dominio.valueobject.PasswordHash;
//import sigmacine.dominio.entity.*;
import java.lang.Long;

public class Cliente extends Usuario {
    /*private String nombre;
    private String fechaRegistro; // podría ser LocalDate
    private SigmaCard sigmaCard;  
    private List<Compra> compras; // historial de compras

    public Cliente(Long id, Email email, PasswordHash passwordHash, String nombre, String fechaRegistro) {
        super(id, email, passwordHash, Usuario.ROL_CLIENTE);
        this.nombre = nombre;
        this.fechaRegistro = fechaRegistro;
        this.sigmaCard = new SigmaCard();
        this.compras = new ArrayList<>();
    }*/
    private String nombre;
    private String fechaRegistro;
    private SigmaCard sigmaCard;
    private List<Compra> compras = new ArrayList<>(); // Inicialización directa

    public Cliente(Long id, Email email, PasswordHash passwordHash, String nombre, String fechaRegistro, SigmaCard sigmaCard) {
        super(id, email, passwordHash, Usuario.ROL_CLIENTE);
        this.nombre = nombre;
        this.fechaRegistro = fechaRegistro;
        this.sigmaCard = new SigmaCard(); // Esto se puede mover al constructor sin problema
    }
    
    public Cliente(Long id, Email email, PasswordHash passwordHash, String nombre, String fechaRegistro)
    {
        super(id, email, passwordHash, Usuario.ROL_CLIENTE);
        this.nombre = nombre;
        this.fechaRegistro = fechaRegistro;
    }

    /*public Cliente(Long id, Email email, PasswordHash passwordHash, String nombre, String fechaRegistro, SigmaCard sigmaCard) {
        super(id, email, passwordHash, Usuario.ROL_CLIENTE);
        this.nombre = nombre;
        this.fechaRegistro = fechaRegistro;
        this.sigmaCard = new SigmaCard(); // Esto se puede mover al constructor sin problema
    } */

    // --- Comportamientos de dominio ---
    public void agregarCompra(Compra compra) {
        this.compras.add(compra);
       // this.sigmaCard.acumularPuntos(compra.getTotal());
    }

    public void recargarSigmaCard(BigDecimal monto) {
        this.sigmaCard.recargar(monto);
    }

    public BigDecimal consultarSaldoSigmaCard() {
        return this.sigmaCard.getSaldo();
    }

    // Getters
    public String getNombre() { return nombre; }
    public String getFechaRegistro() { return fechaRegistro; }
    public SigmaCard getSigmaCard() { return sigmaCard; }
    public List<Compra> getCompras() { return compras; }
}
