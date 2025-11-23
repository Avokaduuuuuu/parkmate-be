package com.parkmate.rating.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

@Schema(
        description = "Request body for updating an existing rating. All fields are optional - only include fields you want to update.",
        example = """
                {
                  "overallRating": 4,
                  "title": "Updated: Good experience",
                  "comment": "After returning multiple times, I've had consistently good experiences.",
                  "isVisible": true
                }
                """
)
public record RatingUpdateRequest(
        @Schema(
                description = "Updated overall star rating (1-5). Leave null to keep current value.",
                example = "4",
                minimum = "1",
                maximum = "5",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED
        )
        @Min(value = 1, message = "Rating must be at least 1")
        @Max(value = 5, message = "Rating must not exceed 5")
        Integer overallRating,

        @Schema(
                description = "Updated review title. Leave null to keep current value.",
                example = "Updated: Good experience",
                maxLength = 200,
                requiredMode = Schema.RequiredMode.NOT_REQUIRED
        )
        @Size(max = 200, message = "Title must not exceed 200 characters")
        String title,

        @Schema(
                description = "Updated detailed comment. Leave null to keep current value.",
                example = "After returning multiple times, I've had consistently good experiences.",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED
        )
        String comment,

        @Schema(
                description = "Update visibility status. Set to false to hide rating (moderation). Leave null to keep current value.",
                example = "true",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED
        )
        Boolean isVisible
) {
}