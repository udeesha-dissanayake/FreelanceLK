package lk.freelance.backend.controller;

//This is made for testing without jwt. for testing only. nothing to do with the project itself

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @GetMapping("/test")
    public String testApi() {
        return "API is working!";
    }
}