package com.pequelandia.pequelandia.util;

import jakarta.servlet.http.HttpSession;

/** [Doc]
 * Obtiene, crea o vacía el carrito almacenado en HttpSession.
 */
public class CartSessionUtil {

    // [Doc] Atributo KEY (String): clave de atributo usada para localizar el valor en la sesión. Se inicializa con "CART_SESSION". Su referencia no se reasigna después de inicializarse.
    public static final String KEY = "CART_SESSION";

    /** [Doc]
     * Devuelve or create, según el cuerpo o la implementación del contrato.
     * @param session sesión HTTP que conserva identidad, carrito y token CSRF.
     * @return CartSession con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    public static CartSession getOrCreate(HttpSession session) {
        // [Doc] Variable local cart (CartSession): carrito en memoria de la sesión. Se inicializa con (CartSession) session.getAttribute(KEY).
        CartSession cart = (CartSession) session.getAttribute(KEY);
        // [Doc] Evalúa cart == null. Si es verdadero, ejecuta su rama; de lo contrario continúa o ejecuta else.
        if (cart == null) {
            // [Doc] Ejecuta la llamada o modificación cart = new CartSession(); Sus efectos dependen del método invocado y del objeto receptor.
            cart = new CartSession();
            // [Doc] Almacena un atributo en la sesión o petición: session.setAttribute(KEY, cart);
            session.setAttribute(KEY, cart);
        }
        // [Doc] Termina el método o la función y devuelve cart.
        return cart;
    }

    /** [Doc]
     * Vacía el carrito o elimina su atributo de sesión según la clase.
     * @param session sesión HTTP que conserva identidad, carrito y token CSRF.
     */
    public static void clear(HttpSession session) {
        // [Doc] Ejecuta la llamada o modificación session.removeAttribute(KEY); Sus efectos dependen del método invocado y del objeto receptor.
        session.removeAttribute(KEY);
    }
}