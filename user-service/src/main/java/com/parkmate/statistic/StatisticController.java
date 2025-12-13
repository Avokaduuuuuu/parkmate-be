package com.parkmate.statistic;

import com.parkmate.common.dto.ApiResponse;
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
@RequestMapping("/api/v1/user-service/statistics")
@RequiredArgsConstructor
@Tag(name = "User Service Statistic", description = "APIs for retrieving user service performance and revenue statistics")
public class StatisticController {
    private final StatisticService statisticService;

    @Operation(
            summary = "Get user-service revenue statistics",
            description = """
                    Retrieve the revenue, all status reservation and subscription 
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
    @GetMapping("/{lotId}/revenue")
    public ResponseEntity<?> getRevenueByLotId(
            @PathVariable("lotId") Long lotId,
            @RequestParam("from")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam("to")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to
    ) {
        return ResponseEntity.status(HttpStatus.OK).body(
                ApiResponse.success(
                        "Fetch revenue statistic successfully",
                        statisticService.getUserStatistic(lotId, from, to)
                )
        );
    }

    @GetMapping("/platform/partner")
    public ResponseEntity<?> getPlatformPartner() {
        return ResponseEntity.status(HttpStatus.OK).body(
                ApiResponse.success(
                        "Fetch Partner Statistic successfully",
                        statisticService.getPartnerStatistic()
                )
        );
    }

    @GetMapping("/platform/user")
    public ResponseEntity<?> getPlatformUser(
            @RequestParam("from")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam("to")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to
    ) {
        return ResponseEntity.status(HttpStatus.OK).body(
                ApiResponse.success(
                        "Fetch User Statistic successfully",
                        statisticService.getUserStatistic(from, to)
                )
        );
    }
}
