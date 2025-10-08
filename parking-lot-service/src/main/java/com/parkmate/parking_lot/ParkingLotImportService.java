package com.parkmate.parking_lot;

import com.parkmate.parking_lot.dto.ParkingLotExcelImportDTO;
import com.parkmate.parking_lot.enums.ParkingLotStatus;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ParkingLotImportService {

    private final ParkingLotRepository parkingLotRepository;

    @Transactional
    public ImportResult importFromExcel(MultipartFile file) throws IOException {
        List<ParkingLotEntity> parkingLots = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        int successCount = 0;
        int errorCount = 0;

        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            
            // Skip header row
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                try {
                    ParkingLotExcelImportDTO dto = parseRow(row);
                    ParkingLotEntity entity = convertToEntity(dto);
                    parkingLots.add(entity);
                    
                    // Batch insert every 500 records for better performance
                    if (parkingLots.size() >= 500) {
                        parkingLotRepository.saveAll(parkingLots);
                        successCount += parkingLots.size();
                        parkingLots.clear();
                        log.info("Imported {} records so far", successCount);
                    }
                } catch (Exception e) {
                    errorCount++;
                    errors.add("Row " + (i + 1) + ": " + e.getMessage());
                    log.error("Error importing row {}: {}", i + 1, e.getMessage());
                }
            }

            // Save remaining records
            if (!parkingLots.isEmpty()) {
                parkingLotRepository.saveAll(parkingLots);
                successCount += parkingLots.size();
            }
        }

        return ImportResult.builder()
                .successCount(successCount)
                .errorCount(errorCount)
                .errors(errors)
                .build();
    }

    private ParkingLotExcelImportDTO parseRow(Row row) {
        return ParkingLotExcelImportDTO.builder()
                .partnerId(getLongValue(row.getCell(0)))
                .name(getStringValue(row.getCell(1)))
                .streetAddress(getStringValue(row.getCell(2)))
                .ward(getStringValue(row.getCell(3)))
                .city(getStringValue(row.getCell(4)))
                .latitude(getDoubleValue(row.getCell(5)))
                .longitude(getDoubleValue(row.getCell(6)))
                .totalFloors(getIntegerValue(row.getCell(7)))
                .operatingHoursStart(getTimeValue(row.getCell(8)))
                .operatingHoursEnd(getTimeValue(row.getCell(9)))
                .is24Hour(getBooleanValue(row.getCell(10)))
                .boundaryTopLeftX(getDoubleValue(row.getCell(11)))
                .boundaryTopLeftY(getDoubleValue(row.getCell(12)))
                .boundaryWidth(getDoubleValue(row.getCell(13)))
                .boundaryHeight(getDoubleValue(row.getCell(14)))
                .status(getStatusValue(row.getCell(15)))
                .reason(getStringValue(row.getCell(16)))
                .build();
    }

    private ParkingLotEntity convertToEntity(ParkingLotExcelImportDTO dto) {
        return ParkingLotEntity.builder()
                .partnerId(dto.getPartnerId())
                .name(dto.getName())
                .streetAddress(dto.getStreetAddress())
                .ward(dto.getWard())
                .city(dto.getCity())
                .latitude(dto.getLatitude())
                .longitude(dto.getLongitude())
                .totalFloors(dto.getTotalFloors())
                .operatingHoursStart(dto.getOperatingHoursStart())
                .operatingHoursEnd(dto.getOperatingHoursEnd())
                .is24Hour(dto.getIs24Hour())
                .boundaryTopLeftX(dto.getBoundaryTopLeftX())
                .boundaryTopLeftY(dto.getBoundaryTopLeftY())
                .boundaryWidth(dto.getBoundaryWidth())
                .boundaryHeight(dto.getBoundaryHeight())
                .status(dto.getStatus())
                .reason(dto.getReason())
                .build();
    }

    // Helper methods to safely extract cell values
    private String getStringValue(Cell cell) {
        if (cell == null) return null;
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> String.valueOf((long) cell.getNumericCellValue());
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            default -> null;
        };
    }

    private Long getLongValue(Cell cell) {
        if (cell == null) return null;
        return switch (cell.getCellType()) {
            case NUMERIC -> (long) cell.getNumericCellValue();
            case STRING -> Long.parseLong(cell.getStringCellValue());
            default -> null;
        };
    }

    private Integer getIntegerValue(Cell cell) {
        if (cell == null) return null;
        return switch (cell.getCellType()) {
            case NUMERIC -> (int) cell.getNumericCellValue();
            case STRING -> Integer.parseInt(cell.getStringCellValue());
            default -> null;
        };
    }

    private Double getDoubleValue(Cell cell) {
        if (cell == null) return null;
        return switch (cell.getCellType()) {
            case NUMERIC -> cell.getNumericCellValue();
            case STRING -> Double.parseDouble(cell.getStringCellValue());
            default -> null;
        };
    }

    private Boolean getBooleanValue(Cell cell) {
        if (cell == null) return null;
        return switch (cell.getCellType()) {
            case BOOLEAN -> cell.getBooleanCellValue();
            case STRING -> Boolean.parseBoolean(cell.getStringCellValue());
            case NUMERIC -> cell.getNumericCellValue() != 0;
            default -> null;
        };
    }

    private LocalTime getTimeValue(Cell cell) {
        if (cell == null) return null;
        try {
            if (cell.getCellType() == CellType.STRING) {
                String timeStr = cell.getStringCellValue();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
                return LocalTime.parse(timeStr, formatter);
            } else if (cell.getCellType() == CellType.NUMERIC) {
                return cell.getLocalDateTimeCellValue().toLocalTime();
            }
        } catch (Exception e) {
            log.error("Error parsing time value: {}", e.getMessage());
        }
        return null;
    }

    private ParkingLotStatus getStatusValue(Cell cell) {
        if (cell == null) return ParkingLotStatus.PENDING;
        try {
            String statusStr = getStringValue(cell);
            return statusStr != null ? ParkingLotStatus.valueOf(statusStr) : ParkingLotStatus.PENDING;
        } catch (IllegalArgumentException e) {
            log.error("Invalid status value: {}", getStringValue(cell));
            return ParkingLotStatus.PENDING;
        }
    }

    @Data
    @Builder
    public static class ImportResult {
        private int successCount;
        private int errorCount;
        private List<String> errors;
    }
}