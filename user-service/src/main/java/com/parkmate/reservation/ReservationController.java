package com.parkmate.reservation;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/reservations")
@Tag(name = "Reservation Management", description = "Endpoints for reservation management")
public class ReservationController {


}
