package es.iessoterohernandez.daw.endes.CuentaTarjeta;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.Date;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class TestCredito {
	
	// Sesión de cobertura del 100% para Credito
	// Sesión de cobertura del 26,7% para Cuenta
	// Sesión de cobertura del 100% para Movimiento
	// Sesión de cobertura del 100% para Tarjeta
	Credito credito;
	
	final String titular = "MArta", MEDIAMARKT = "MEDIAMARKT";
	
	@SuppressWarnings("deprecation")
	@BeforeEach
	void init() {
		credito = new Credito("1", titular, new Date(2024, 1, 1), 200D);
		credito.setCuenta(new Cuenta("123456789", titular));
	}
	
	@AfterEach
	void finish() {
		credito = null;
	}
	
	@ParameterizedTest(name = "Ingresar {0}€")
	@MethodSource("cantidades")
	void testIngresarCreditoConCuenta(double cantidad) {
		try {
			credito.ingresar(cantidad);
			assertEquals(credito.mCuentaAsociada.mMovimientos.size(),
						 credito.mMovimientos.size()
			);
		} catch (Exception ex) {
			System.out.println(ex.getMessage());
		}
	}
	
	@ParameterizedTest(name = "Ingresar {0}€")
	@MethodSource("cantidades")
	void testIngresarCreditoSinCuenta(double cantidad) {
		// NullPointerException
		credito.setCuenta(null);
		try {
			credito.ingresar(cantidad);
		} catch (Exception ex) {
			System.out.println(ex.getMessage());
		}
	}
	
	@ParameterizedTest(name = "Retirar {0}€")
	@MethodSource("cantidades")
	void testRetirarCreditoConCuenta(double cantidad) {
		try {
			credito.retirar(cantidad);
			assertNotEquals(credito.mCuentaAsociada.mMovimientos.size(),
							credito.mMovimientos.size()
			);
		} catch (Exception ex) {
			System.out.println(ex.getMessage());
		}
	}
	
	@ParameterizedTest(name = "Retirar {0}€")
	@MethodSource("cantidades")
	void testRetirarCreditoSinCuenta(double cantidad) {
		try {
			credito.retirar(cantidad);
			assertTrue(credito.getCreditoDisponible() > 0);
		} catch (Exception ex) {
			// Crédito insuficiente
			System.out.println(ex.getMessage());
		}
	}
	
	@ParameterizedTest(name = "Pagar {0}€ en " + MEDIAMARKT)
	@MethodSource("cantidades")
	void testPagoEnEstablecimiento(double cantidad) {
		try {
			credito.pagoEnEstablecimiento(MEDIAMARKT, cantidad);
			assertThat(credito.mMovimientos.size(), is(1));
		} catch (Exception ex) {
			System.out.println(ex.getMessage());
		}
	}
	
	@Test
	void testSaldoTotal() {
        for (Double cantidad : cantidades().toArray(Double[]::new)) {
            try {
                credito.ingresar(cantidad);
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
        }
        assertEquals(credito.mMovimientos.size(), cantidades().count());
		double saldo = cantidades()
	                   .mapToDouble(Double::doubleValue)
	                   .sum();
        assertThat(credito.getSaldo(), is(saldo));
	}
	
	@ParameterizedTest(name = "Liquidación en mes {0} de {1}")
	@MethodSource("mesesAnos")
    void testLiquidacion(int mes, int ano) {
		Date[] fechas = fechas().toArray(Date[]::new);
    	for (int i = 0; i < fechas.length; i++) {
            try {
                credito.ingresar(80D);
                ((Movimiento) credito.mMovimientos.get(i)).setFecha(fechas[i]);
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
        }
    	int numMovimientosAntes = credito.mCuentaAsociada.mMovimientos.size();
        credito.liquidar(mes, ano);
        int numMovimientosDespues = credito.mCuentaAsociada.mMovimientos.size();
        Movimiento ultimo = (Movimiento) credito.mCuentaAsociada.mMovimientos.lastElement();
        if (numMovimientosAntes < numMovimientosDespues)
        	assertTrue(ultimo.getImporte() > 0);
        else // No se ha realizado ninguna operación de liquidación...
        	assertFalse(ultimo.getConcepto().contains("Liquidación"));
    }
	
	static Stream<Double> cantidades() {
		return Stream.of(0D, 50D, 100D, 2000D, 5000D);
	}
	
	static Stream<LocalDate> fechasLocales() {
		return Stream.of(
			LocalDate.of(2024, 1, 1),
			LocalDate.of(2024, 1, 2),
			LocalDate.of(2024, 1, 3),
			LocalDate.of(2024, 2, 1),
			LocalDate.of(2024, 2, 2),
			LocalDate.of(2025, 1, 1)
		);
	}
	
	static Stream<Date> fechas() {
		return fechasLocales().map(fecha -> java.sql.Date.valueOf(fecha));
	}
	
	static Stream<Arguments> mesesAnos() {
		return Stream.of(
			Arguments.of(1, 2024),
			Arguments.of(2, 2024),
			Arguments.of(3, 2024),
			Arguments.of(1, 2025),
			Arguments.of(2, 2025),
			Arguments.of(3, 2025)
		);
	}

}