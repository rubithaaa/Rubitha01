package com.foodapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;
import jakarta.persistence.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.*;

@SpringBootApplication
public class FoodDeliveryApplication {
    public static void main(String[] args) {
        SpringApplication.run(FoodDeliveryApplication.class, args);
    }
}

/* ===================== ENTITIES ===================== */

@Entity
class Restaurant {
    @Id @GeneratedValue
    Long id;
    String name, cuisine, location;
}

@Entity
class FoodItem {
    @Id @GeneratedValue
    Long id;
    String name;
    double price;
    Long restaurantId;
}

@Entity
class OrderDetails {
    @Id @GeneratedValue
    Long id;
    String customer;
    Long restaurantId;
    String status;
    String driverLocation;
    int eta; // minutes
    double amount;
}

@Entity
class Review {
    @Id @GeneratedValue
    Long id;
    String user;
    String comment;
    int rating;
}

/* ===================== REPOSITORIES ===================== */

interface RestaurantRepo extends JpaRepository<Restaurant,Long>{}
interface FoodRepo extends JpaRepository<FoodItem,Long>{
    List<FoodItem> findByRestaurantId(Long id);
}
interface OrderRepo extends JpaRepository<OrderDetails,Long>{}
interface ReviewRepo extends JpaRepository<Review,Long>{}

/* ===================== CONTROLLER ===================== */

@RestController
class FoodController {

    @Autowired RestaurantRepo restaurantRepo;
    @Autowired FoodRepo foodRepo;
    @Autowired OrderRepo orderRepo;
    @Autowired ReviewRepo reviewRepo;

    /* ---------- RESTAURANT SIGNUP ---------- */
    @PostMapping("/restaurant/signup")
    public String restaurantSignup(@RequestParam String name,
                                   @RequestParam String cuisine,
                                   @RequestParam String location){
        Restaurant r = new Restaurant();
        r.name=name; r.cuisine=cuisine; r.location=location;
        restaurantRepo.save(r);
        return "Restaurant Registered";
    }

    /* ---------- ADD MENU ---------- */
    @PostMapping("/restaurant/menu")
    public String addFood(@RequestParam Long restaurantId,
                          @RequestParam String food,
                          @RequestParam double price){
        foodRepo.save(new FoodItem(null,food,price,restaurantId));
        return "Food Item Added";
    }

    /* ---------- VIEW RESTAURANTS ---------- */
    @GetMapping("/restaurants")
    public List<Restaurant> restaurants(){
        return restaurantRepo.findAll();
    }

    /* ---------- VIEW MENU ---------- */
    @GetMapping("/menu/{restaurantId}")
    public List<FoodItem> menu(@PathVariable Long restaurantId){
        return foodRepo.findByRestaurantId(restaurantId);
    }

    /* ---------- PLACE ORDER (CART + PAYMENT) ---------- */
    @PostMapping("/order")
    public String placeOrder(@RequestParam String customer,
                             @RequestParam Long restaurantId,
                             @RequestParam double amount){
        OrderDetails o = new OrderDetails();
        o.customer=customer;
        o.restaurantId=restaurantId;
        o.amount=amount;
        o.status="Order Placed";
        o.driverLocation="Restaurant";
        o.eta=30;
        orderRepo.save(o);
        return "Order Placed Successfully";
    }

    /* ---------- RESTAURANT ACCEPT & ASSIGN DRIVER ---------- */
    @PostMapping("/restaurant/accept")
    public String acceptOrder(@RequestParam Long orderId){
        OrderDetails o = orderRepo.findById(orderId).get();
        o.status="Out for Delivery";
        o.driverLocation="2 km away";
        o.eta=15;
        orderRepo.save(o);
        return "Driver Assigned";
    }

    /* ---------- TRACK ORDER ---------- */
    @GetMapping("/track/{orderId}")
    public OrderDetails track(@PathVariable Long orderId){
        return orderRepo.findById(orderId).get();
    }

    /* ---------- DELIVERY UPDATE ---------- */
    @PostMapping("/driver/update")
    public String update(@RequestParam Long orderId,
                         @RequestParam String location,
                         @RequestParam int eta){
        OrderDetails o = orderRepo.findById(orderId).get();
        o.driverLocation=location;
        o.eta=eta;
        orderRepo.save(o);
        return "Location Updated";
    }

    /* ---------- REVIEW ---------- */
    @PostMapping("/review")
    public String review(@RequestParam String user,
                         @RequestParam String comment,
                         @RequestParam int rating){
        Review r = new Review();
        r.user=user; r.comment=comment; r.rating=rating;
        reviewRepo.save(r);
        return "Review Submitted";
    }
}
