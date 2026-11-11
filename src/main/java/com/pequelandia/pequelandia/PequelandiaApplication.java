package com.pequelandia.pequelandia;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/** [Doc]
 * Inicia Spring Boot y habilita el descubrimiento de componentes y las tareas programadas.
 */
@SpringBootApplication
public class PequelandiaApplication {

    /** [Doc]
     * Inicia la aplicación Spring Boot con los argumentos de la línea de comandos.
     * @param args valores vinculados a los parámetros de consulta.
     */
	public static void main(String[] args) {
        // [Doc] Ejecuta la llamada o modificación SpringApplication.run(PequelandiaApplication.class, args); Sus efectos dependen del método invocado y del objeto receptor.
		SpringApplication.run(PequelandiaApplication.class, args);
	}

}
