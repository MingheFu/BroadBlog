package com.broadblog.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class OrderService {

    @Autowired
    private UserService userService;

    public void processOrder() {
        System.out.println("Processing order...");
        userService.sayHello();
    }
}
