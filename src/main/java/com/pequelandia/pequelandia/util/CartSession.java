package com.pequelandia.pequelandia.util;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/** [Doc]
 * Mantiene en memoria de sesión un mapa de producto a cantidad y permite obtener copias del carrito.
 */
public class CartSession implements Serializable {

    // [Doc] Atributo items (Map<Long, Integer>): elementos del carrito o de la colección procesada. Se inicializa con new HashMap<>(). Su referencia no se reasigna después de inicializarse.
    private final Map<Long, Integer> items = new HashMap<>(); // productoId -> cantidad

    /** [Doc]
     * Devuelve una copia no modificable del mapa de productos y cantidades.
     * @return Map&lt;Long, Integer&gt; con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    public synchronized Map<Long, Integer> getItems() {
        // [Doc] Termina el método o la función y devuelve Map.copyOf(items).
        return Map.copyOf(items);
    }

    /** [Doc]
     * Suma las cantidades de todas las líneas del carrito; no cuenta solo los productos distintos.
     * @return int con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    public synchronized int getTotalItems() {
        // [Doc] Termina el método o la función y devuelve items.values().stream().mapToInt(Integer::intValue).sum().
        return items.values().stream().mapToInt(Integer::intValue).sum();
    }

    /** [Doc]
     * Exige ID y cantidad positiva y acumula unidades con detección de desbordamiento mediante Math.addExact.
     * @param productoId identificador del producto.
     * @param cantidad unidades de una línea de carrito.
     */
    public synchronized void add(Long productoId, int cantidad) {
        // [Doc] Evalúa productoId == null. Si es verdadero, ejecuta su rama; de lo contrario continúa o ejecuta else.
        if (productoId == null) throw new IllegalArgumentException("productoId requerido");
        // [Doc] Evalúa cantidad <= 0. Si es verdadero, ejecuta su rama; de lo contrario continúa o ejecuta else.
        if (cantidad <= 0) throw new IllegalArgumentException("cantidad debe ser > 0");
        // [Doc] Ejecuta la llamada o modificación items.merge(productoId, cantidad, Math::addExact); Sus efectos dependen del método invocado y del objeto receptor.
        items.merge(productoId, cantidad, Math::addExact);
        // si queda <=0 por alguna razón, lo sacamos
        // [Doc] Evalúa items.get(productoId) <= 0. Si es verdadero, ejecuta su rama; de lo contrario continúa o ejecuta else.
        if (items.get(productoId) <= 0) items.remove(productoId);
    }

    /** [Doc]
     * Asigna la cantidad del producto; si es cero o negativa, elimina la línea.
     * @param productoId identificador del producto.
     * @param cantidad unidades de una línea de carrito.
     */
    public synchronized void set(Long productoId, int cantidad) {
        // [Doc] Evalúa productoId == null. Si es verdadero, ejecuta su rama; de lo contrario continúa o ejecuta else.
        if (productoId == null) throw new IllegalArgumentException("productoId requerido");
        // [Doc] Evalúa cantidad <= 0. Si es verdadero, ejecuta su rama; de lo contrario continúa o ejecuta else.
        if (cantidad <= 0) items.remove(productoId);
        else items.put(productoId, cantidad);
    }

    /** [Doc]
     * Elimina del mapa la línea del producto indicado.
     * @param productoId identificador del producto.
     */
    public synchronized void remove(Long productoId) {
        // [Doc] Evalúa productoId != null. Si es verdadero, ejecuta su rama; de lo contrario continúa o ejecuta else.
        if (productoId != null) items.remove(productoId);
    }

    /** [Doc]
     * Vacía el carrito o elimina su atributo de sesión según la clase.
     */
    public synchronized void clear() {
        // [Doc] Ejecuta la llamada o modificación items.clear(); Sus efectos dependen del método invocado y del objeto receptor.
        items.clear();
    }

    /** [Doc]
     * Indica si el mapa del carrito no contiene líneas.
     * @return boolean con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    public synchronized boolean isEmpty() {
        // [Doc] Termina el método o la función y devuelve items.isEmpty().
        return items.isEmpty();
    }
    /** [Doc]
     * Devuelve una copia del mapa del carrito para procesar la compra sin compartir el mapa mutable de la sesión.
     * @return Map&lt;Long, Integer&gt; con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    public synchronized Map<Long, Integer> snapshot() { return Map.copyOf(items); }
}