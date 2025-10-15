package com.epam.rd.autocode.spring.project.controller;

import com.epam.rd.autocode.spring.project.dto.BookItemDTO;
import com.epam.rd.autocode.spring.project.dto.Cart;
import com.epam.rd.autocode.spring.project.service.CartService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {
    private final CartService cartService;

    @GetMapping
    public String viewCart(@CookieValue(name = "shoppingCart", required = false) String cartJson, Model model) {
        Cart cart = cartService.getCartFromCookies(cartJson);
        List<BookItemDTO> cartDetails = cartService.getDetailedCartItems(cart);

        model.addAttribute("cart", cart);
        model.addAttribute("cartDetails", cartDetails);

        return "user/view-cart";
    }

    @PostMapping("/add")
    public String addToCart(@RequestParam Long bookId,
                            @RequestParam(defaultValue = "1") int quantity,
                            @CookieValue(name = "shoppingCart", required = false) String cartJson,
                            HttpServletResponse response) {

        Cart cart = cartService.getCartFromCookies(cartJson);
        Cart updatedCart = cartService.addBookToCart(cart, bookId, quantity);
        setCartCookie(response, updatedCart);

        return "redirect:/cart";
    }

    @PostMapping("/remove/{bookId}")
    public String removeFromCart(@PathVariable Long bookId,
                                 @CookieValue(name = "shoppingCart", required = false) String cartJson,
                                 HttpServletResponse response) {

        Cart cart = cartService.getCartFromCookies(cartJson);
        Cart updatedCart = cartService.removeItemFromCart(cart, bookId);
        setCartCookie(response, updatedCart);

        return "redirect:/cart";
    }

    @PostMapping("/checkout")
    @PreAuthorize("hasRole('CUSTOMER')")
    public String checkout(@CookieValue(name = "shoppingCart", required = false) String cartJson,
                           @AuthenticationPrincipal Jwt jwt,
                           HttpServletResponse response,
                           Model model) {

        if (jwt == null) {
            return "redirect:/login?redirect=/cart";
        }

        Cart cart = cartService.getCartFromCookies(cartJson);

        try {
            Long userId = Long.parseLong(jwt.getClaimAsString("jti"));

            cartService.checkout(cart, userId);

            Cookie cookie = new Cookie(CartService.CART_COOKIE_NAME, "");
            cookie.setMaxAge(0);
            cookie.setPath("/");
            response.addCookie(cookie);

            return "redirect:/books";

        } catch (Exception e) {
            model.addAttribute("errorMessage", "Помилка оформлення замовлення: " + e.getMessage());
            return "user/view-cart";
        }
    }

    private void setCartCookie(HttpServletResponse response, Cart cart) {
        String cartJson = cartService.serializeCart(cart);
        Cookie cookie = new Cookie(CartService.CART_COOKIE_NAME, cartJson);
        cookie.setPath("/");
        cookie.setMaxAge(60 * 60);
        cookie.setHttpOnly(true);
        response.addCookie(cookie);
    }
}
