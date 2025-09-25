package com.parkmate.spot.dto.req;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record SpotCreateRequest(
    @NotNull(message = "Lot name must not be null")
    @NotEmpty(message = "Lot name must not be empty")
    String name,
    @NotNull(message = "Lot top left X must not be null")
    Double lotTopLeftX,
    @NotNull(message = "Lot top left Y must not be null")
    Double lotTopLeftY,
    @NotNull(message = "Lot width must not be null")
    Double lotWidth,
    @NotNull(message = "Lot height must not be null")
    Double lotHeight
) {
}
