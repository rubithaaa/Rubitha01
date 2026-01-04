package com.store;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;
import jakarta.persistence.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.*;

@SpringBootApplication
public class DepartmentalStoreApplication {
    public static void main(String[] args) {
        SpringApplication.run(DepartmentalStoreApplication.class, args);
    }
}

/* ===================== ENTITIES ===================== */

@Entity
class GroceryItem {
    @Id @GeneratedValue
    Long id;
    String name;
    int quantity;
    double price;
}

@Entity
class User {
    @Id @GeneratedValue
    Long id;
    String name;
    String address;
}

@Entity
class OrderDetails {
    @Id @GeneratedValue
    Long id;
    Long userId;
    String deliverySlot;
    String paymentMode;
    String status;
    String driverLocation;
    int eta;
    double amount;
}

/* ===================== REPOSITORIES ===================== */

interface GroceryRepo extends JpaRepository<GroceryItem,Long>{}
interface UserRepo extends JpaRepository<User,Long>{}
interface OrderRepo extends JpaRepository<OrderDetails,Long>{}

/* ===================== CONTROLLER ===================== */

@RestController
class StoreController {

    @Autowired GroceryRepo groceryRepo;
    @Autowired UserRepo userRepo;
    @Autowired OrderRepo orderRepo;

    /* ---------- ADMIN: ADD GROCERY ---------- */
    @PostMapping("/admin/addItem")
    public String addItem(@RequestParam String name,
                          @RequestParam int qty,
                          @RequestParam double price){
        GroceryItem g = new GroceryItem();
        g.name=name; g.quantity=qty; g.price=price;
        groceryRepo.save(g);
        return "Item Added to Inventory";
    }

    /* ---------- VIEW ITEMS ---------- */
    @GetMapping("/items")
    public List<GroceryItem> items(){
        return groceryRepo.findAll();
    }

    /* ---------- USER SIGNUP ---------- */
    @PostMapping("/user/signup")
    public String signup(@RequestParam String name,
                         @RequestParam String address){
        User u = new User();
        u.name=name; u.address=address;
        userRepo.save(u);
        return "User Registered";
    }

    /* ---------- PLACE ORDER ---------- */
    @PostMapping("/order")
    public String order(@RequestParam Long userId,
                        @RequestParam double amount,
                        @RequestParam String slot,
                        @RequestParam String payment){
        OrderDetails o = new OrderDetails();
        o.userId=userId;
        o.amount=amount;
        o.deliverySlot=slot;
        o.paymentMode=payment;
        o.status="Order Placed";
        o.driverLocation="Warehouse";
        o.eta=60;
        orderRepo.save(o);
        return "Order Placed Successfully";
    }

    /* ---------- ASSIGN DELIVERY ---------- */
    @PostMapping("/admin/assignDelivery")
    public String assign(@RequestParam Long orderId){
        OrderDetails o = orderRepo.findById(orderId).get();
        o.status="Out for Delivery";
        o.driverLocation="5 km away";
        o.eta=30;
        orderRepo.save(o);
        return "Delivery Assigned";
    }

    /* ---------- TRACK ORDER ---------- */
    @GetMapping("/track/{orderId}")
    public OrderDetails track(@PathVariable Long orderId){
        return orderRepo.findById(orderId).get();
    }

    /* ---------- DRIVER UPDATE ---------- */
    @PostMapping("/driver/update")
    public String update(@RequestParam Long orderId,
                         @RequestParam String location,
                         @RequestParam int eta){
        OrderDetails o = orderRepo.findById(orderId).get();
        o.driverLocation=location;
        o.eta=eta;
        orderRepo.save(o);
        return "Driver Status Updated";
    }

    /* ---------- ANALYTICS ---------- */
    @GetMapping("/admin/analytics")
    public String analytics(){
        return "Total Orders: " + orderRepo.count() +
               ", Total Products: " + groceryRepo.count();
    }
}
