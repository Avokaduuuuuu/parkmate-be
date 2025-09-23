package com.parkmate.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/parking-lot-service/parking-lots")
public class ParkingLotController {

    @GetMapping
    public ResponseEntity<?> hello() {
        return ResponseEntity.ok("Hello World");
    }
}
