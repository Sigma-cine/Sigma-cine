-- Usuarios, Clientes y Administradores
INSERT INTO USUARIO (ID, EMAIL, CONTRASENA) VALUES(1, 'admin@sigma.com', 'admin_pass');
INSERT INTO USUARIO (ID, EMAIL, CONTRASENA) VALUES(2, 'cliente1@correo.com', 'cliente1_pass');
INSERT INTO USUARIO (ID, EMAIL, CONTRASENA) VALUES(3, 'cliente2@correo.com', 'cliente2_pass');

INSERT INTO ADMIN (ID, NOMBRE) VALUES(1, 'Juan Perez');

INSERT INTO CLIENTE (ID, NOMBRE, FECHA_REGISTRO) VALUES(2, 'Ana Gomez', '2025-09-15');
INSERT INTO CLIENTE (ID, NOMBRE, FECHA_REGISTRO) VALUES(3, 'Carlos Rodriguez', '2025-09-15');

---
-- Películas, Salas y Tarifas
INSERT INTO PELICULA (ID, TITULO, GENERO, CLASIFICACION, DURACION, DIRECTOR, REPARTO, TRAILER, SINOPSIS, ESTADO) VALUES(1, 'Dune: Part Two', 'Ciencia ficción', 'PG-13', 166, 'Denis Villeneuve', 'Timothée Chalamet, Zendaya', 'https://www.youtube.com/watch?v=Way9Dexny3w', 'Sigue el viaje mítico de Paul Atreides mientras se une a Chani y los Fremen en una guerra de venganza contra los conspiradores que destruyeron a su familia.', 'En Cartelera');
INSERT INTO PELICULA (ID, TITULO, GENERO, CLASIFICACION, DURACION, DIRECTOR, REPARTO, TRAILER, SINOPSIS, ESTADO) VALUES(2, 'Joker', 'Thriller psicológico', 'R', 122, 'Todd Phillips', 'Joaquin Phoenix, Robert De Niro', 'https://www.youtube.com/watch?v=zL2Q9C0cK5s', 'Un comediante fracasado desciende a la locura y se convierte en una figura icónica del crimen.', 'En Cartelera');
INSERT INTO PELICULA (ID, TITULO, GENERO, CLASIFICACION, DURACION, DIRECTOR, REPARTO, TRAILER, SINOPSIS, ESTADO) VALUES(3, 'Godzilla x Kong: The New Empire', 'Acción, Ciencia ficción', 'PG-13', 115, 'Adam Wingard', 'Rebecca Hall, Brian Tyree Henry', 'https://www.youtube.com/watch?v=qqj12z9f48E', 'Kong y Godzilla se unen para luchar contra una amenaza colosal desconocida que se esconde en nuestro mundo.', 'Próximamente');

INSERT INTO SALA (ID, NUMERO_SALA, CAPACIDAD, TIPO) VALUES(1, 101, 150, '2D');
INSERT INTO SALA (ID, NUMERO_SALA, CAPACIDAD, TIPO) VALUES(2, 102, 100, '3D');
INSERT INTO SALA (ID, NUMERO_SALA, CAPACIDAD, TIPO) VALUES(3, 103, 80, 'VIP');

INSERT INTO TARIFA (ID, NOMBRE, PRECIO_BASE, VIGENCIA) VALUES(1, 'Entrada General 2D', 15.00, '2025-01-01');
INSERT INTO TARIFA (ID, NOMBRE, PRECIO_BASE, VIGENCIA) VALUES(2, 'Entrada General 3D', 18.00, '2025-01-01');
INSERT INTO TARIFA (ID, NOMBRE, PRECIO_BASE, VIGENCIA) VALUES(3, 'Entrada VIP', 25.00, '2025-01-01');

---
-- Productos y Funciones
INSERT INTO PRODUCTO (ID, NOMBRE, DESCRIPCION, TIPO, PRECIO_LISTA, ESTADO) VALUES(1, 'Combo Popcorn', 'Popcorn grande y refresco grande', 'COMIDA', 10.50, 'Disponible');
INSERT INTO PRODUCTO (ID, NOMBRE, DESCRIPCION, TIPO, PRECIO_LISTA, ESTADO) VALUES(2, 'Hot Dog', 'Hot Dog Clásico con papas', 'COMIDA', 8.75, 'Disponible');
INSERT INTO PRODUCTO (ID, NOMBRE, DESCRIPCION, TIPO, PRECIO_LISTA, ESTADO) VALUES(3, 'Nachos', 'Nachos con queso derretido y jalapeños', 'COMIDA', 9.25, 'Disponible');
INSERT INTO PRODUCTO (ID, NOMBRE, DESCRIPCION, TIPO, PRECIO_LISTA, ESTADO) VALUES(4, 'Agua', 'Botella de agua (500ml)', 'BEBIDA', 3.00, 'Disponible');

INSERT INTO FUNCION (ID, FECHA, HORA, ESTADO, PELICULA_ID, SALA_ID) VALUES(1, '2025-09-15', '18:00', 'Activa', 1, 1); -- Dune en sala 101
INSERT INTO FUNCION (ID, FECHA, HORA, ESTADO, PELICULA_ID, SALA_ID) VALUES(2, '2025-09-15', '20:30', 'Activa', 2, 2); -- Joker en sala 102
INSERT INTO FUNCION (ID, FECHA, HORA, ESTADO, PELICULA_ID, SALA_ID) VALUES(3, '2025-09-16', '15:00', 'Activa', 1, 3); -- Dune en sala 103

---
-- Sillas (Relacionadas a las Salas)
INSERT INTO SILLA (ID, FILA, NUMERO, SALA_ID) VALUES(1, 'A', 1, 1);
INSERT INTO SILLA (ID, FILA, NUMERO, SALA_ID) VALUES(2, 'A', 2, 1);
INSERT INTO SILLA (ID, FILA, NUMERO, SALA_ID) VALUES(3, 'B', 1, 1);
INSERT INTO SILLA (ID, FILA, NUMERO, SALA_ID) VALUES(4, 'A', 1, 2);
INSERT INTO SILLA (ID, FILA, NUMERO, SALA_ID) VALUES(5, 'A', 2, 2);
INSERT INTO SILLA (ID, FILA, NUMERO, SALA_ID) VALUES(6, 'B', 1, 3);


-- Reservas, Compras y Pagos
INSERT INTO COMPRA (ID, TOTAL, FECHA, CLIENTE_ID) VALUES(1, 19.50, '2025-09-15', 2);

INSERT INTO PAGO (ID, METODO, MONTO, ESTADO, COMPRA_ID) VALUES(1, 'Tarjeta de crédito', 19.50, 'Completado', 1);

INSERT INTO RESERVA (ID, CODIGO, FECHA_VENCIMIENTO, ESTADO, PRECIO_FINAL, CLIENTE_ID, FUNCION_ID) VALUES(1, 'RSV-001', '2025-09-15', 'Pendiente', 30.00, 2, 1);

INSERT INTO BOLETO (ID, CODIGO, ESTADO, PRECIO_FINAL, COMPRA_ID, FUNCION_ID) VALUES(1, 'BOL-001', 'Usado', 15.00, 1, 1);
INSERT INTO BOLETO (ID, CODIGO, ESTADO, PRECIO_FINAL, COMPRA_ID, FUNCION_ID) VALUES(2, 'BOL-002', 'Usado', 15.00, 1, 1);

---
-- Tablas de Relaciones (muchos a muchos)
INSERT INTO RESERVA_SILLA (RESERVA_ID, SILLA_ID) VALUES(1, 2);
INSERT INTO RESERVA_SILLA (RESERVA_ID, SILLA_ID) VALUES(1, 3);

INSERT INTO BOLETO_SILLA (BOLETO_ID, SILLA_ID) VALUES(1, 1);
INSERT INTO BOLETO_SILLA (BOLETO_ID, SILLA_ID) VALUES(2, 4);

INSERT INTO COMPRA_PRODUCTO (COMPRA_ID, PRODUCTO_ID, CANTIDAD, PRECIO_UNITARIO, SUBTOTAL) VALUES(1, 1, 1, 10.50, 10.50);
INSERT INTO COMPRA_PRODUCTO (COMPRA_ID, PRODUCTO_ID, CANTIDAD, PRECIO_UNITARIO, SUBTOTAL) VALUES(1, 4, 3, 3.00, 9.00);

