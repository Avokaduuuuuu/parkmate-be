package com.parkmate.user.dto;

import com.parkmate.partner.dto.ImportError;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ImportUserResponse {
    private int totalRows;
    private int successCount;
    private List<ImportError> errors = new ArrayList<>();

    public void addError(ImportError error) {
        this.errors.add(error);
    }
}
