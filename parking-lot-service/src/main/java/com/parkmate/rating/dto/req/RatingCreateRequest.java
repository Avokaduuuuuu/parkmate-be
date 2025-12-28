package com.parkmate.rating.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(
        description = "Request body for creating a new parking lot rating",
        example = """
                {
                  "userId": 1001,
                  "overallRating": 5,
                  "title": "Excellent parking facility",
                  "comment": "Very clean and well-maintained. Security is great!"
                }
                """
)
public record RatingCreateRequest(
        @Schema(
                description = "Overall star rating for the parking lot (1-5)",
                example = "5",
                minimum = "1",
                maximum = "5",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull(message = "Overall rating must not be null")
        @Min(value = 1, message = "Rating must be at least 1")
        @Max(value = 5, message = "Rating must not exceed 5")
        Integer overallRating,

        @Schema(
                description = "Short title/summary of the review",
                example = "Excellent parking facility",
                maxLength = 200,
                requiredMode = Schema.RequiredMode.NOT_REQUIRED
        )
        @Size(max = 200, message = "Title must not exceed 200 characters")
        String title,

        @Schema(
                description = "Detailed review comment",
                example = "Very clean and well-maintained. Security is great! Easy entry and exit with BLE. Highly recommend for daily parking.",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED
        )
        String comment
) {
}