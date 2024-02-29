package es.iessoterohernandez.daw.endes.CuentaTarjeta;

import java.util.Date;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class TestDebito {
	
	// Sesión de cobertura del 60,2% para Cuenta
	// Sesión de cobertura del 100% para Debito
	// Sesión de cobertura del 65,5% para Movimiento
	// Sesión de cobertura del 100% para Tarjeta
	Debito debito;
	
	final String titular = "Marta", MEDIAMARKT = "MEDIAMARKT";
	
	@SuppressWarnings("deprecation")
	@BeforeEach
	void init() {
		debito = new Debito("1", titular, new Date(2024, 1, 1));
		debito.setCuenta(new Cuenta("123456789", titular));
	}
	
	@AfterEach
	void finish() {
		debito = null;
	}
	
	@ParameterizedTest(name = "Ingresar {0}€")
	@MethodSource("cantidades")
	void testIngresarDebitoConCuenta(double cantidad) {
		try {
			debito.ingresar(cantidad);
			assertThat(debito.mCuentaAsociada.mMovimientos.size(), is(1));
		} catch (Exception ex) {
			System.out.println(ex.getMessage());
		}
	}
	
	@ParameterizedTest(name = "Ingresar {0}€")
	@MethodSource("cantidades")
	void testIngresarDebitoSinCuenta(double cantidad) {
		// NullPointerException
		debito.setCuenta(null);
		try {
			debito.ingresar(cantidad);
		} catch (Exception ex) {
			System.out.println(ex.getMessage());
		}
	}
	
	@ParameterizedTest(name = "Retirar {0}€")
	@MethodSource("cantidades")
	void testRetirarDebitoConCuenta(double cantidad) {
		try {
			debito.ingresar(80D);
			debito.retirar(cantidad);
		} catch (Exception ex) {
			System.out.println(ex.getMessage());
		}
	}
	
	@ParameterizedTest(name = "Retirar {0}€")
	@MethodSource("cantidades")
	void testRetirarDebitoSinCuenta(double cantidad) {
		try {
			debito.ingresar(80D);
			debito.retirar(cantidad);
		} catch (Exception ex) {
			// Crédito insuficiente
			System.out.println(ex.getMessage());
		}
	}
	
	@ParameterizedTest(name = "Pagar {0}€ en " + MEDIAMARKT)
	@MethodSource("cantidades")
	void testPagoEnEstablecimiento(double cantidad) {
		try {
			debito.ingresar(80D);
			debito.pagoEnEstablecimiento(MEDIAMARKT, cantidad);
			assertThat(debito.mCuentaAsociada.mMovimientos.size(), is(2));
		} catch (Exception ex) {
			System.out.println(ex.getMessage());
		}
	}
	
	@Test
	void testSaldoTotal() {
        for (Double cantidad : cantidades().toArray(Double[]::new)) {
            try {
                debito.ingresar(cantidad);
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
        }
		double saldo = cantidades()
	                   .mapToDouble(Double::doubleValue)
	                   .sum();
        assertThat(debito.getSaldo(), is(saldo));
	}
	
	static Stream<Double> cantidades() {
		return Stream.of(0D, 50D, 100D, -100D);
	}

}