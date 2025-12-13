package com.parkmate.statistic;

import com.parkmate.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/parking-service/statistics")
@RequiredArgsConstructor
@Tag(name = "Parking Lot Statistics", description = "APIs for retrieving parking lot performance and revenue statistics")
public class StatisticController {

    private final StatisticService statisticService;

    @Operation(
            summary = "Get parking lot statistics",
            description = """
                    Retrieve the revenue, completed sessions, and active sessions 
                    for a specific parking lot within a given time range.
                    """,
            parameters = {
                    @Parameter(
                            name = "lotId",
                            description = "Unique ID of the parking lot",
                            required = true,
                            example = "101"
                    ),
                    @Parameter(
                            name = "from",
                            description = "Start of the time range (ISO 8601 format)",
                            required = true,
                            example = "2025-11-09T00:00:00"
                    ),
                    @Parameter(
                            name = "to",
                            description = "End of the time range (ISO 8601 format)",
                            required = true,
                            example = "2025-11-11T23:59:59"
                    )
            }
    )
    @GetMapping("/{lotId}")
    public ResponseEntity<?> fetchStatisticsOfParkingLot(
            @PathVariable("lotId") Long lotId,
            @RequestParam("from")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam("to")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to
    ) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(
                        ApiResponse.success(
                                "Fetch Statistic of a parking lot successfully",
                                statisticService.getParkingLotStatistic(lotId, from, to)
                        )
                );
    }

    @Operation(
            summary = "Get Platform statistics",
            description = """
                Retrieve comprehensive platform-wide statistics including parking lots, partners, users, and revenue.
                Provides an overview of the entire ParkMate ecosystem for admin dashboard and business analytics.
                
                **Statistics Included:**
                - **Lots**: Total, active, pending, under maintenance, preparing
                - **Partners**: Total, active, suspended, pending registrations
                - **Users**: Total and new users in the specified period
                - **Revenue**: Operational fees, subscriptions, reservations, sessions with growth rates
                
                **Authorization:** Admin only - contains sensitive business metrics
                
                **Use Cases:**
                - Admin dashboard overview
                - Business performance monitoring
                - Executive reports and analytics
                - Growth tracking and trend analysis
                """,
            parameters = {
                    @Parameter(
                            name = "from",
                            description = "Start of the time range (ISO 8601 format). Used for calculating new users and revenue in the period.",
                            required = true,
                            example = "2025-11-01T00:00:00"
                    ),
                    @Parameter(
                            name = "to",
                            description = "End of the time range (ISO 8601 format). Used for calculating new users and revenue in the period.",
                            required = true,
                            example = "2025-11-30T23:59:59"
                    )
            }
    )
    @GetMapping("/platform")
    public ResponseEntity<?> fetchStatisticsOfPlatform(
            @RequestParam("from")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam("to")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to
    ) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(
                        ApiResponse.success(
                                "Fetch Statistic of Platform successfully",
                                statisticService.getPlatformOverviewStatistic(from, to)
                        )
                );
    }
}
