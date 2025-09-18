package sigmacine.dominio.valueobject;

public final class Email {
    private final String value;

    public Email(String raw) {
        if (raw == null) throw new IllegalArgumentException("email null");
        String v = raw.trim().toLowerCase();
        if (!v.matches("^[\\w._%+-]+@[\\w.-]+\\.[A-Za-z]{2,}$"))
            throw new IllegalArgumentException("email inválido");
        this.value = v;
    }

    public String value() { return value; }
    @Override public String toString() { return value; }
    @Override public boolean equals(Object o){ return (o instanceof Email e) && e.value.equals(this.value); }
    @Override public int hashCode(){ return value.hashCode(); }
}
