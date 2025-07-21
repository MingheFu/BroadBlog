package org.example;

import org.springframework.stereotype.Component;

@Component
public class UserService {
    public void sayHello() {
        System.out.println("Hello from UserService!");
    }
}
