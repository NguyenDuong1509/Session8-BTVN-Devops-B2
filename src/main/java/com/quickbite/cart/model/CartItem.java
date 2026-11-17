package com.quickbite.cart.model;

import java.math.BigDecimal;

public class CartItem {

    private String itemId;
    private String name;
    private int quantity;
    private BigDecimal price;

    public CartItem() {
    }

    public CartItem(String itemId, String name, int quantity, BigDecimal price) {
        this.itemId = itemId;
        this.name = name;
        this.quantity = quantity;
        this.price = price;
    }

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }
}
