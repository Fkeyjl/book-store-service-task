package com.epam.rd.autocode.spring.project.service.impl;

import com.epam.rd.autocode.spring.project.dto.BookDTO;
import com.epam.rd.autocode.spring.project.dto.BookItemDTO;
import com.epam.rd.autocode.spring.project.dto.Cart;
import com.epam.rd.autocode.spring.project.exception.CartValidationException;
import com.epam.rd.autocode.spring.project.service.BookService;
import com.epam.rd.autocode.spring.project.service.CartService;
import com.epam.rd.autocode.spring.project.service.OrderService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {
    private final BookService bookService;
    private final OrderService orderService;
    private final ObjectMapper objectMapper;
    
    private static final int MAX_QUANTITY_PER_ITEM = 100;
    private static final int MAX_TOTAL_ITEMS = 50;

    public String serializeCart(Cart cart) {
        try {
            String json = objectMapper.writeValueAsString(cart);
            return URLEncoder.encode(json, StandardCharsets.UTF_8);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize cart", e);
        }
    }

    public Cart deserializeCart(String cartJson) {
        if (cartJson == null || cartJson.isEmpty()) {
            return new Cart();
        }
        try {
            String json = URLDecoder.decode(cartJson, StandardCharsets.UTF_8);
            return objectMapper.readValue(json, Cart.class);
        } catch (JsonProcessingException e) {
            log.warn("Failed to deserialize cart, creating new empty cart", e);
            return new Cart();
        }
    }

    @Override
    public Cart getCartFromCookies(String cartJson) {
        Cart cart = deserializeCart(cartJson);
        recalculateCart(cart);
        return cart;
    }

    public Cart addBookToCart(Cart cart, Long bookId, int quantity) {
        if (quantity <= 0) {
            throw new CartValidationException("Quantity must be positive");
        }
        if (quantity > MAX_QUANTITY_PER_ITEM) {
            throw new CartValidationException("Quantity exceeds maximum allowed (" + MAX_QUANTITY_PER_ITEM + ")");
        }

        bookService.getBookById(bookId);

        if (!cart.getItems().containsKey(bookId) && cart.getItems().size() >= MAX_TOTAL_ITEMS) {
            throw new CartValidationException("Cart is full. Maximum " + MAX_TOTAL_ITEMS + " different items allowed");
        }
        
        cart.getItems().merge(bookId, quantity, Integer::sum);

        Integer currentQuantity = cart.getItems().get(bookId);
        if (currentQuantity > MAX_QUANTITY_PER_ITEM) {
            cart.getItems().put(bookId, MAX_QUANTITY_PER_ITEM);
            log.warn("Item {} quantity limited to {}", bookId, MAX_QUANTITY_PER_ITEM);
        }
        recalculateCart(cart);
        return cart;
    }

    public Cart removeItemFromCart(Cart cart, Long bookId) {
        cart.getItems().remove(bookId);
        recalculateCart(cart);
        return cart;
    }

    public void recalculateCart(Cart cart) {
        if (cart.getItems().isEmpty()) {
            cart.setTotalQuantity(0);
            cart.setTotalPrice(BigDecimal.ZERO);
            return;
        }
        List<Long> bookIds = new ArrayList<>(cart.getItems().keySet());
        List<BookDTO> books = bookService.getBooksByIds(bookIds);

        Map<Long, BookDTO> booksMap = books.stream()
                .collect(toMap(BookDTO::getId, book -> book));
        
        int totalQuantity = 0;
        BigDecimal totalPrice = BigDecimal.ZERO;

        cart.getItems().entrySet().removeIf(entry -> {
            BookDTO book = booksMap.get(entry.getKey());
            if (book == null) {
                log.warn("Book {} not found, removing from cart", entry.getKey());
                return true;
            }
            return false;
        });

        for (Map.Entry<Long, Integer> entry : cart.getItems().entrySet()) {
            Long bookId = entry.getKey();
            Integer quantity = entry.getValue();
            
            BookDTO book = booksMap.get(bookId);
            if (book != null) {
                totalQuantity += quantity;
                totalPrice = totalPrice.add(book.getPrice().multiply(BigDecimal.valueOf(quantity)));
            }
        }

        cart.setTotalQuantity(totalQuantity);
        cart.setTotalPrice(totalPrice);
    }

    public List<BookItemDTO> getDetailedCartItems(Cart cart) {
        if (cart.getItems().isEmpty()) {
            return List.of();
        }
        List<Long> bookIds = new ArrayList<>(cart.getItems().keySet());
        List<BookDTO> books = bookService.getBooksByIds(bookIds);

        Map<Long, BookDTO> booksMap = books.stream()
                .collect(toMap(BookDTO::getId, book -> book));
        
        List<BookItemDTO> detailedItems = new ArrayList<>();

        for (Map.Entry<Long, Integer> entry : cart.getItems().entrySet()) {
            Long bookId = entry.getKey();
            Integer quantity = entry.getValue();

            BookDTO book = booksMap.get(bookId);
            if (book != null) {
                BookItemDTO dto = new BookItemDTO(book, quantity);
                detailedItems.add(dto);
            } else {
                log.warn("Book {} not found when getting detailed cart items", bookId);
            }
        }
        return detailedItems;
    }

    @Transactional
    public void checkout(Cart cart, Long userId) {
        if (cart.getItems().isEmpty()) {
            throw new IllegalStateException("The cart is empty, it is impossible to place an order.");
        }
        orderService.createOrderFromCart(cart, userId);
        cart.getItems().clear();
        recalculateCart(cart);
    }
}
