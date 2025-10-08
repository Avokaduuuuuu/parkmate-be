package com.parkmate.partner.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@Builder
public class ImportPartnerResponse {

    private int totalRows;
    private int successCount;
    private int failedCount;
    private List<ImportError> errors;

    public ImportPartnerResponse() {
        this.errors = new ArrayList<>();
    }

    public void addError(ImportError error) {
        this.errors.add(error);
        this.failedCount++;
    }
}