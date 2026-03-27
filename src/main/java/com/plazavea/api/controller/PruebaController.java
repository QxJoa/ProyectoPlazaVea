package com.plazavea.api.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PruebaController {

    @GetMapping("/hola")
    public String hola() {
        return "API Plaza Vea funcionando";
    }
}

