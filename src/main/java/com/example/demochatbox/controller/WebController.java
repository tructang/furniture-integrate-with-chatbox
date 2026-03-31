package com.example.demochatbox.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebController {

    @GetMapping("/")
    public String home() {
        return "app";
    }

    @GetMapping("/products/{slug}")
    public String detail() {
        return "app";
    }
}
