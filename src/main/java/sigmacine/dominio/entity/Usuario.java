package sigmacine.dominio.entity;

import sigmacine.dominio.valueobject.*;
//import sigmacine.dominio.valueobject.PasswordHash;

public class Usuario {
    public static final String ROL_CLIENTE = "CLIENTE";
    public static final String ROL_ADMIN   = "ADMIN";

    public static final String ESTADO_ACTIVO    = "ACTIVO";
    public static final String ESTADO_BLOQUEADO = "BLOQUEADO";
    public static final String ESTADO_INACTIVO  = "INACTIVO";

    private Long id;
    private Email email;
    private PasswordHash passwordHash;
    private String rol;
    private String estado;
    private int intentosFallidos;
    private boolean mfaHabilitado;

    protected Usuario(Long id, Email email, PasswordHash passwordHash, String rol) {
        if (!ROL_CLIENTE.equals(rol) && !ROL_ADMIN.equals(rol))
            throw new IllegalArgumentException("Rol inválido: " + rol);
        this.id = id;
        this.email = email;
        this.passwordHash = passwordHash;
        this.rol = rol;
        this.estado = ESTADO_ACTIVO;
        this.intentosFallidos = 0;
        this.mfaHabilitado = ROL_ADMIN.equals(rol);
    }

    public boolean autenticar(String plainPassword) {
        return this.passwordHash != null && this.passwordHash.matches(plainPassword);
    }

    public void registrarIntentoFallido() {
        this.intentosFallidos++;
        if (this.intentosFallidos >= 3) this.estado = ESTADO_BLOQUEADO;
    }
    public void limpiarIntentosFallidos() { this.intentosFallidos = 0; }
    public boolean puedeAutenticarse() { return ESTADO_ACTIVO.equals(this.estado); }
    public boolean esAdmin()   { return ROL_ADMIN.equals(this.rol); }
    public boolean esCliente() { return ROL_CLIENTE.equals(this.rol); }
    public void habilitarMfa()   { if (esAdmin()) this.mfaHabilitado = true; }
    public void deshabilitarMfa(){ this.mfaHabilitado = false; }

    // Getters
    public Long getId() { return id; }
    public Email getEmail() { return email; }
    public PasswordHash getPasswordHash() { return passwordHash; }
    public String getRol() { return rol; }
    public String getEstado() { return estado; }
    public int getIntentosFallidos() { return intentosFallidos; }
}
