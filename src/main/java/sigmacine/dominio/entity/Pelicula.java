package sigmacine.dominio.entity;


public class Pelicula {
	private int id;
	private String titulo;
	private String genero;
	private String clasificacion;
	private String duracion;
	private String direccion;
	private java.util.List<String> reparto;
	private String trailer;
	private String sinopsis;
	private boolean estado;

	public Pelicula() {}

	public Pelicula(int id, String titulo, String genero, String clasificacion, String duracion, String direccion, java.util.List<String> reparto, String trailer, String sinopsis, boolean estado) {
		this.id = id;
		this.titulo = titulo;
		this.genero = genero;
		this.clasificacion = clasificacion;
		this.duracion = duracion;
		this.direccion = direccion;
		this.reparto = reparto;
		this.trailer = trailer;
		this.sinopsis = sinopsis;
		this.estado = estado;
	}

	public int getId() { return id; }
	public void setId(int id) { this.id = id; }

	public String getTitulo() { return titulo; }
	public void setTitulo(String titulo) { this.titulo = titulo; }

	public String getGenero() { return genero; }
	public void setGenero(String genero) { this.genero = genero; }

	public String getClasificacion() { return clasificacion; }
	public void setClasificacion(String clasificacion) { this.clasificacion = clasificacion; }

	public String getDuracion() { return duracion; }
	public void setDuracion(String duracion) { this.duracion = duracion; }

	public String getDireccion() { return direccion; }
	public void setDireccion(String direccion) { this.direccion = direccion; }

	public java.util.List<String> getReparto() { return reparto; }
	public void setReparto(java.util.List<String> reparto) { this.reparto = reparto; }

	public String getTrailer() { return trailer; }
	public void setTrailer(String trailer) { this.trailer = trailer; }

	public String getSinopsis() { return sinopsis; }
	public void setSinopsis(String sinopsis) { this.sinopsis = sinopsis; }

	public boolean isEstado() { return estado; }
	public void setEstado(boolean estado) { this.estado = estado; }
}
