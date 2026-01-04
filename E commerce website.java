package com.autoparts;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;
import jakarta.persistence.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.*;

@SpringBootApplication
public class AutoPartsEcommerceApplication {
    public static void main(String[] args) {
        SpringApplication.run(AutoPartsEcommerceApplication.class, args);
    }
}

/* ===================== ENTITIES ===================== */

@Entity
class Product {
    @Id @GeneratedValue
    Long id;
    String name, category, specs;
    double price;
    int stock;

    public Product(){}
    public Product(String n,String c,String s,double p,int st){
        name=n; category=c; specs=s; price=p; stock=st;
    }
}

@Entity
class CartItem {
    @Id @GeneratedValue
    Long id;
    Long productId;
    int quantity;
}

@Entity
class OrderDetails {
    @Id @GeneratedValue
    Long id;
    String customer;
    double totalAmount;
    String paymentStatus;
}

/* ===================== REPOSITORIES ===================== */

interface ProductRepo extends JpaRepository<Product,Long>{
    List<Product> findByCategory(String category);
}
interface CartRepo extends JpaRepository<CartItem,Long>{}
interface OrderRepo extends JpaRepository<OrderDetails,Long>{}

/* ===================== CONTROLLER ===================== */

@RestController
class EcommerceController {

    @Autowired ProductRepo productRepo;
    @Autowired CartRepo cartRepo;
    @Autowired OrderRepo orderRepo;

    /* ---------- ADMIN: ADD PRODUCT ---------- */
    @PostMapping("/admin/addProduct")
    public String addProduct(@RequestParam String name,
                             @RequestParam String category,
                             @RequestParam String specs,
                             @RequestParam double price,
                             @RequestParam int stock){
        productRepo.save(new Product(name,category,specs,price,stock));
        return "Product Added Successfully";
    }

    /* ---------- BROWSE PRODUCTS ---------- */
    @GetMapping("/products")
    public List<Product> viewProducts(){
        return productRepo.findAll();
    }

    @GetMapping("/products/{category}")
    public List<Product> byCategory(@PathVariable String category){
        return productRepo.findByCategory(category);
    }

    /* ---------- ADD TO CART ---------- */
    @PostMapping("/cart/add")
    public String addToCart(@RequestParam Long productId,
                            @RequestParam int qty){
        CartItem c = new CartItem();
        c.productId = productId;
        c.quantity = qty;
        cartRepo.save(c);
        return "Item Added to Cart";
    }

    /* ---------- VIEW CART ---------- */
    @GetMapping("/cart")
    public List<CartItem> viewCart(){
        return cartRepo.findAll();
    }

    /* ---------- CHECKOUT & PAYMENT ---------- */
    @PostMapping("/checkout")
    public String checkout(@RequestParam String customer){
        double total = 0;
        for (CartItem c : cartRepo.findAll()) {
            Product p = productRepo.findById(c.productId).get();
            total += p.price * c.quantity;
        }
        OrderDetails o = new OrderDetails();
        o.customer = customer;
        o.totalAmount = total;
        o.paymentStatus = "PAID via Stripe (Mock)";
        orderRepo.save(o);
        cartRepo.deleteAll();
        return "Order Confirmed. Email Sent. Amount: ₹" + total;
    }

    /* ---------- ORDER HISTORY ---------- */
    @GetMapping("/orders")
    public List<OrderDetails> orders(){
        return orderRepo.findAll();
    }
}
