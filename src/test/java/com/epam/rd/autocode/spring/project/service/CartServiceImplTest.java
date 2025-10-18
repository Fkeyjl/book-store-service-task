package com.epam.rd.autocode.spring.project.service;

import com.epam.rd.autocode.spring.project.dto.BookDTO;
import com.epam.rd.autocode.spring.project.dto.BookItemDTO;
import com.epam.rd.autocode.spring.project.dto.Cart;
import com.epam.rd.autocode.spring.project.service.impl.CartServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceImplTest {

    @Mock
    private BookService bookService;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private CartServiceImpl cartService;

    private Cart testCart;
    private BookDTO testBook;

    @BeforeEach
    void setUp() {
        testCart = new Cart();
        testCart.setItems(new HashMap<>());
        testCart.setTotalPrice(BigDecimal.ZERO);

        testBook = new BookDTO();
        testBook.setId(1L);
        testBook.setName("Test Book");
        testBook.setPrice(BigDecimal.valueOf(20.00));
    }

    @Test
    void testAddBookToCart_ShouldAddNewBook() {
        when(bookService.getBookById(1L)).thenReturn(testBook);
        when(bookService.getBooksByIds(anyList())).thenReturn(List.of(testBook));

        cartService.addBookToCart(testCart, 1L, 2);

        assertThat(testCart.getItems()).containsKey(1L);
        assertThat(testCart.getItems().get(1L)).isEqualTo(2);
        assertThat(testCart.getTotalPrice()).isEqualByComparingTo(BigDecimal.valueOf(40.00));
        verify(bookService, times(1)).getBookById(1L);
    }

    @Test
    void testAddBookToCart_ShouldIncreaseQuantity() {
        testCart.getItems().put(1L, 1);
        when(bookService.getBookById(1L)).thenReturn(testBook);
        when(bookService.getBooksByIds(anyList())).thenReturn(List.of(testBook));

        cartService.addBookToCart(testCart, 1L, 2);

        assertThat(testCart.getItems().get(1L)).isEqualTo(3);
        verify(bookService, times(1)).getBookById(1L);
    }

    @Test
    void testUpdateBookQuantity_ShouldUpdateQuantity() {
        testCart.getItems().put(1L, 5);
        when(bookService.getBooksByIds(anyList())).thenReturn(List.of(testBook));

        cartService.updateBookQuantity(testCart, 1L, 3);

        assertThat(testCart.getItems().get(1L)).isEqualTo(3);
    }

    @Test
    void testRemoveItemFromCart_ShouldRemoveItem() {
        testCart.getItems().put(1L, 2);
        testCart.getItems().put(2L, 1);
        
        BookDTO testBook2 = new BookDTO();
        testBook2.setId(2L);
        testBook2.setName("Test Book 2");
        testBook2.setPrice(BigDecimal.valueOf(15.00));
        
        when(bookService.getBooksByIds(anyList())).thenReturn(List.of(testBook, testBook2));

        cartService.removeItemFromCart(testCart, 1L);

        assertThat(testCart.getItems()).doesNotContainKey(1L);
        assertThat(testCart.getItems()).containsKey(2L);
    }

    @Test
    void testRecalculateCart_ShouldUpdateTotalPrice() {
        BookDTO book1 = new BookDTO();
        book1.setId(1L);
        book1.setPrice(BigDecimal.valueOf(10.00));

        BookDTO book2 = new BookDTO();
        book2.setId(2L);
        book2.setPrice(BigDecimal.valueOf(15.00));

        testCart.getItems().put(1L, 2);
        testCart.getItems().put(2L, 3);

        when(bookService.getBooksByIds(anyList())).thenReturn(List.of(book1, book2));

        cartService.recalculateCart(testCart);

        assertThat(testCart.getTotalPrice()).isEqualByComparingTo(BigDecimal.valueOf(65.00));
        verify(bookService, times(1)).getBooksByIds(anyList());
    }

    @Test
    void testGetCartFromCookies_WithNullOrEmptyString_ShouldReturnEmptyCart() {
        Cart result1 = cartService.getCartFromCookies(null);
        Cart result2 = cartService.getCartFromCookies("");

        assertThat(result1).isNotNull();
        assertThat(result1.getItems()).isEmpty();
        assertThat(result2).isNotNull();
        assertThat(result2.getItems()).isEmpty();
    }

    @Test
    void testGetDetailedCartItems_ShouldReturnDetailedItems() {
        Map<Long, Integer> items = new HashMap<>();
        items.put(1L, 2);
        testCart.setItems(items);

        BookItemDTO bookItemDTO = new BookItemDTO();
        bookItemDTO.setBook(testBook);
        bookItemDTO.setQuantity(2);

        when(bookService.getBooksByIds(anyList())).thenReturn(List.of(testBook));

        List<BookItemDTO> result = cartService.getDetailedCartItems(testCart);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getQuantity()).isEqualTo(2);
    }
}
