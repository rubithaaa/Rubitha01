package com.marketplace;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;
import jakarta.persistence.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.*;

@SpringBootApplication
public class MarketplaceApplication {
    public static void main(String[] args) {
        SpringApplication.run(MarketplaceApplication.class, args);
    }
}

/* ===================== ENTITIES ===================== */

@Entity
class Merchant {
    @Id @GeneratedValue
    Long id;
    String name, category, description;

    public Merchant(){}
    public Merchant(String n,String c,String d){
        name=n; category=c; description=d;
    }
}

@Entity
class ServiceItem {
    @Id @GeneratedValue
    Long id;
    String serviceName;
    double price;
    Long merchantId;

    public ServiceItem(){}
    public ServiceItem(String s,double p,Long m){
        serviceName=s; price=p; merchantId=m;
    }
}

@Entity
class OrderDetails {
    @Id @GeneratedValue
    Long id;
    String customerName;
    String paymentStatus;
    double amount;
}

/* ===================== REPOSITORIES ===================== */

interface MerchantRepo extends JpaRepository<Merchant,Long>{}
interface ServiceRepo extends JpaRepository<ServiceItem,Long>{
    List<ServiceItem> findByMerchantId(Long id);
}
interface OrderRepo extends JpaRepository<OrderDetails,Long>{}

/* ===================== CONTROLLER ===================== */

@RestController
class MarketplaceController {

    @Autowired MerchantRepo merchantRepo;
    @Autowired ServiceRepo serviceRepo;
    @Autowired OrderRepo orderRepo;

    /* ---------- MERCHANT SIGNUP ---------- */
    @PostMapping("/merchant/signup")
    public String merchantSignup(@RequestParam String name,
                                 @RequestParam String category,
                                 @RequestParam String desc){
        merchantRepo.save(new Merchant(name,category,desc));
        return "Merchant Registered Successfully";
    }

    /* ---------- ADD SERVICES ---------- */
    @PostMapping("/merchant/service")
    public String addService(@RequestParam Long merchantId,
                             @RequestParam String service,
                             @RequestParam double price){
        serviceRepo.save(new ServiceItem(service,price,merchantId));
        return "Service Added";
    }

    /* ---------- BROWSE BY CATEGORY ---------- */
    @GetMapping("/browse/{category}")
    public List<Merchant> browse(@PathVariable String category){
        return merchantRepo.findAll()
                .stream().filter(m->m.category.equalsIgnoreCase(category))
                .toList();
    }

    /* ---------- VIEW SERVICES ---------- */
    @GetMapping("/services/{merchantId}")
    public List<ServiceItem> services(@PathVariable Long merchantId){
        return serviceRepo.findByMerchantId(merchantId);
    }

    /* ---------- CHECKOUT (PAYMENT GATEWAY SIMULATION) ---------- */
    @PostMapping("/checkout")
    public String checkout(@RequestParam String customer,
                           @RequestParam double amount){
        OrderDetails o = new OrderDetails();
        o.customerName = customer;
        o.amount = amount;
        o.paymentStatus = "PAID (Gateway Success)";
        orderRepo.save(o);
        return "Payment Successful. Email Receipt Sent.";
    }

    /* ---------- MERCHANT DASHBOARD ---------- */
    @GetMapping("/merchant/orders")
    public List<OrderDetails> dashboard(){
        return orderRepo.findAll();
    }

    /* ---------- REVIEWS ---------- */
    @PostMapping("/review")
    public String review(@RequestParam String customer,
                         @RequestParam String review){
        return "Review submitted by " + customer;
    }
}
