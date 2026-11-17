package com.quickbite.cart.controller;

import com.quickbite.cart.model.CartItem;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final List<CartItem> cartItems = new ArrayList<>();

    @GetMapping
    public ResponseEntity<List<CartItem>> getCartItems() {
        return ResponseEntity.ok(cartItems);
    }

    @PostMapping("/items")
    public ResponseEntity<CartItem> addItem(@RequestBody CartItem item) {
        cartItems.add(item);
        return new ResponseEntity<>(item, HttpStatus.CREATED);
    }

    @DeleteMapping
    public ResponseEntity<Void> clearCart() {
        cartItems.clear();
        return ResponseEntity.noContent().build();
    }
}
