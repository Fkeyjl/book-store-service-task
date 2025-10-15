package com.epam.rd.autocode.spring.project.service;

import com.epam.rd.autocode.spring.project.dto.BookItemDTO;
import com.epam.rd.autocode.spring.project.dto.Cart;

import java.util.List;

public interface CartService {
    String CART_COOKIE_NAME = "shoppingCart";

    String serializeCart(Cart cart);
    Cart deserializeCart(String cartJson);
    Cart getCartFromCookies(String cartJson);
    Cart addBookToCart(Cart cart, Long bookId, int quantity);
    Cart removeItemFromCart(Cart cart, Long bookId);
    void recalculateCart(Cart cart);
    void checkout(Cart cart, Long userId);
    List<BookItemDTO> getDetailedCartItems(Cart cart);
}
