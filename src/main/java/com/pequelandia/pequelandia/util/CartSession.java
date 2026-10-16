package com.pequelandia.pequelandia.util;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class CartSession implements Serializable {

    private final Map<Long, Integer> items = new HashMap<>(); // productoId -> cantidad

    public Map<Long, Integer> getItems() {
        return items;
    }

    public int getTotalItems() {
        return items.values().stream().mapToInt(Integer::intValue).sum();
    }

    public void add(Long productoId, int cantidad) {
        if (productoId == null) throw new IllegalArgumentException("productoId requerido");
        if (cantidad <= 0) throw new IllegalArgumentException("cantidad debe ser > 0");
        items.merge(productoId, cantidad, Integer::sum);
        // si queda <=0 por alguna razón, lo sacamos
        if (items.get(productoId) <= 0) items.remove(productoId);
    }

    public void set(Long productoId, int cantidad) {
        if (productoId == null) throw new IllegalArgumentException("productoId requerido");
        if (cantidad <= 0) items.remove(productoId);
        else items.put(productoId, cantidad);
    }

    public void remove(Long productoId) {
        if (productoId != null) items.remove(productoId);
    }

    public void clear() {
        items.clear();
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }
}