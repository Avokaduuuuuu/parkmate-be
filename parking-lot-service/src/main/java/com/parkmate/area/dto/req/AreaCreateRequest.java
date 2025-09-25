package com.parkmate.area.dto.req;

import com.parkmate.common.enums.VehicleType;
import com.parkmate.spot.dto.req.SpotCreateRequest;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record AreaCreateRequest(
        @NotNull(message = "Area name must not be null")
        @NotEmpty(message = "Area name must not be empty")
        String name,
        @NotNull(message = "Vehicle Type must not be null")
        @NotEmpty(message = "Vehicle Type must not be empty")
        VehicleType vehicleType,
        @NotNull(message = "Area Top Left X must not be null")
        Double areaTopLeftX,
        @NotNull(message = "Area Top Left Y must not be null")
        Double areaTopLeftY,
        @NotNull(message = "Area Width must not be null")
        Double areaWidth,
        @NotNull(message = "Area Height must not be null")
        Double areaHeight,
        @NotNull(message = "Must define electric vehicle allowance")
        Double isElectric,
        List<SpotCreateRequest> lotRequests
) {
}
