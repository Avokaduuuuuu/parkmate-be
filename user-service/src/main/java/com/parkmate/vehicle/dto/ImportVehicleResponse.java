package com.parkmate.vehicle.dto;

import com.parkmate.partner.dto.ImportError;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ImportVehicleResponse {
    private int totalRows;
    private int successCount;
    private List<ImportError> errors = new ArrayList<>();

    public void addError(ImportError error) {
        this.errors.add(error);
    }
}
