package com.parkmate.statistic;

import com.parkmate.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
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
}
