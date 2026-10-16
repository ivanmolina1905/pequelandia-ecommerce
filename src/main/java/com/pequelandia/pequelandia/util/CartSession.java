package com.pequelandia.pequelandia.util;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class CartSession implements Serializable {

    private final Map<Long, Integer> items = new HashMap<>(); // productoId -> cantidad

    public synchronized Map<Long, Integer> getItems() {
        return Map.copyOf(items);
    }

    public synchronized int getTotalItems() {
        return items.values().stream().mapToInt(Integer::intValue).sum();
    }

    public synchronized void add(Long productoId, int cantidad) {
        if (productoId == null) throw new IllegalArgumentException("productoId requerido");
        if (cantidad <= 0) throw new IllegalArgumentException("cantidad debe ser > 0");
        items.merge(productoId, cantidad, Math::addExact);
        // si queda <=0 por alguna razón, lo sacamos
        if (items.get(productoId) <= 0) items.remove(productoId);
    }

    public synchronized void set(Long productoId, int cantidad) {
        if (productoId == null) throw new IllegalArgumentException("productoId requerido");
        if (cantidad <= 0) items.remove(productoId);
        else items.put(productoId, cantidad);
    }

    public synchronized void remove(Long productoId) {
        if (productoId != null) items.remove(productoId);
    }

    public synchronized void clear() {
        items.clear();
    }

    public synchronized boolean isEmpty() {
        return items.isEmpty();
    }
    public synchronized Map<Long, Integer> snapshot() { return Map.copyOf(items); }
}