package com.pequelandia.pequelandia.util;

import jakarta.servlet.http.HttpSession;

public class CartSessionUtil {

    public static final String KEY = "CART_SESSION";

    public static CartSession getOrCreate(HttpSession session) {
        CartSession cart = (CartSession) session.getAttribute(KEY);
        if (cart == null) {
            cart = new CartSession();
            session.setAttribute(KEY, cart);
        }
        return cart;
    }

    public static void clear(HttpSession session) {
        session.removeAttribute(KEY);
    }
}