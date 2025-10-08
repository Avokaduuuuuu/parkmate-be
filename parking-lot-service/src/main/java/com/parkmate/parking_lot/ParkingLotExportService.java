package com.parkmate.parking_lot;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ParkingLotExportService {

    private final ParkingLotRepository parkingLotRepository;

    /**
     * Export all parking lots to Excel
     */
    public byte[] exportAllToExcel() throws IOException {
        List<ParkingLotEntity> parkingLots = parkingLotRepository.findAll();
        return generateExcel(parkingLots);
    }


    /**
     * Export parking lots by partner ID
     */
    public byte[] exportByPartnerId(Long partnerId) throws IOException {
        List<ParkingLotEntity> parkingLots = parkingLotRepository.findByPartnerId(partnerId);
        return generateExcel(parkingLots);
    }

    /**
     * Export parking lots by status
     */
    public byte[] exportByStatus(com.parkmate.parking_lot.enums.ParkingLotStatus status) throws IOException {
        List<ParkingLotEntity> parkingLots = parkingLotRepository.findByStatus(status);
        return generateExcel(parkingLots);
    }

    /**
     * Generate Excel file from parking lot list
     */
    private byte[] generateExcel(List<ParkingLotEntity> parkingLots) throws IOException {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Parking Lots");

            // Create header style
            CellStyle headerStyle = createHeaderStyle(workbook);
            
            // Create data style
            CellStyle dataStyle = createDataStyle(workbook);

            // Create header row
            createHeaderRow(sheet, headerStyle);

            // Create data rows
            int rowNum = 1;
            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");

            for (ParkingLotEntity parkingLot : parkingLots) {
                Row row = sheet.createRow(rowNum++);

                // ID
                createCell(row, 0, parkingLot.getId(), dataStyle);
                
                // Partner ID
                createCell(row, 1, parkingLot.getPartnerId(), dataStyle);

                // Name
                createCell(row, 2, parkingLot.getName(), dataStyle);

                // Street Address
                createCell(row, 3, parkingLot.getStreetAddress(), dataStyle);

                // Ward
                createCell(row, 4, parkingLot.getWard(), dataStyle);

                // City
                createCell(row, 5, parkingLot.getCity(), dataStyle);

                // Latitude
                createCell(row, 6, parkingLot.getLatitude(), dataStyle);

                // Longitude
                createCell(row, 7, parkingLot.getLongitude(), dataStyle);

                // Total Floors
                createCell(row, 8, parkingLot.getTotalFloors(), dataStyle);

                // Operating Hours Start
                String startTime = parkingLot.getOperatingHoursStart() != null 
                    ? parkingLot.getOperatingHoursStart().format(timeFormatter) 
                    : "";
                createCell(row, 9, startTime, dataStyle);

                // Operating Hours End
                String endTime = parkingLot.getOperatingHoursEnd() != null 
                    ? parkingLot.getOperatingHoursEnd().format(timeFormatter) 
                    : "";
                createCell(row, 10, endTime, dataStyle);

                // Is 24 Hour
                createCell(row, 11, parkingLot.getIs24Hour(), dataStyle);

                // Boundary Top Left X
                createCell(row, 12, parkingLot.getBoundaryTopLeftX(), dataStyle);

                // Boundary Top Left Y
                createCell(row, 13, parkingLot.getBoundaryTopLeftY(), dataStyle);

                // Boundary Width
                createCell(row, 14, parkingLot.getBoundaryWidth(), dataStyle);

                // Boundary Height
                createCell(row, 15, parkingLot.getBoundaryHeight(), dataStyle);

                // Status
                String status = parkingLot.getStatus() != null ? parkingLot.getStatus().name() : "";
                createCell(row, 16, status, dataStyle);

                // Reason
                createCell(row, 17, parkingLot.getReason(), dataStyle);

                // Created At
                String createdAt = parkingLot.getCreatedAt() != null 
                    ? parkingLot.getCreatedAt().toString() 
                    : "";
                createCell(row, 18, createdAt, dataStyle);

                // Updated At
                String updatedAt = parkingLot.getUpdatedAt() != null 
                    ? parkingLot.getUpdatedAt().toString() 
                    : "";
                createCell(row, 19, updatedAt, dataStyle);
            }

            // Auto-size columns
            for (int i = 0; i < 20; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            log.info("Exported {} parking lots to Excel", parkingLots.size());
            return out.toByteArray();
        }
    }

    private void createHeaderRow(Sheet sheet, CellStyle headerStyle) {
        Row headerRow = sheet.createRow(0);

        String[] headers = {
                "ID", "Partner ID", "Name", "Street Address", "Ward", "City",
                "Latitude", "Longitude", "Total Floors",
                "Operating Hours Start", "Operating Hours End", "Is 24 Hour",
                "Boundary Top Left X", "Boundary Top Left Y",
                "Boundary Width", "Boundary Height", "Status", "Reason",
                "Created At", "Updated At"
        };

        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setFontHeightInPoints((short) 12);
        headerFont.setColor(IndexedColors.WHITE.getIndex());
        headerStyle.setFont(headerFont);
        headerStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerStyle.setBorderBottom(BorderStyle.THIN);
        headerStyle.setBorderTop(BorderStyle.THIN);
        headerStyle.setBorderRight(BorderStyle.THIN);
        headerStyle.setBorderLeft(BorderStyle.THIN);
        headerStyle.setAlignment(HorizontalAlignment.CENTER);
        return headerStyle;
    }

    private CellStyle createDataStyle(Workbook workbook) {
        CellStyle dataStyle = workbook.createCellStyle();
        dataStyle.setBorderBottom(BorderStyle.THIN);
        dataStyle.setBorderTop(BorderStyle.THIN);
        dataStyle.setBorderRight(BorderStyle.THIN);
        dataStyle.setBorderLeft(BorderStyle.THIN);
        dataStyle.setWrapText(false);
        return dataStyle;
    }

    private void createCell(Row row, int column, Object value, CellStyle style) {
        Cell cell = row.createCell(column);
        
        if (value == null) {
            cell.setCellValue("");
        } else if (value instanceof String) {
            cell.setCellValue((String) value);
        } else if (value instanceof Integer) {
            cell.setCellValue((Integer) value);
        } else if (value instanceof Long) {
            cell.setCellValue((Long) value);
        } else if (value instanceof Double) {
            cell.setCellValue((Double) value);
        } else if (value instanceof Boolean) {
            cell.setCellValue((Boolean) value);
        } else {
            cell.setCellValue(value.toString());
        }
        
        cell.setCellStyle(style);
    }
}