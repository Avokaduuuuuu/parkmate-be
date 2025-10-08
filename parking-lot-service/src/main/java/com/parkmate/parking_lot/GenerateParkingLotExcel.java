package com.parkmate.parking_lot;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Random;

public class GenerateParkingLotExcel {

    private static final String[] PARKING_LOT_NAMES = {
            "Diamond Plaza Parking", "Vincom Center Parking", "Landmark 81 Parking",
            "Saigon Center Parking", "Parkson Plaza Parking", "Times Square Parking",
            "Golden Plaza Parking", "Sun Plaza Parking", "Star City Parking",
            "Ocean Plaza Parking", "Sky Tower Parking", "Metro Mall Parking",
            "Central Park Parking", "Harbor View Parking", "Riverside Parking",
            "Garden Mall Parking", "Elite Plaza Parking", "Royal Center Parking",
            "Grand Tower Parking", "Victory Plaza Parking"
    };

    private static final String[] STREETS = {
            "Le Duan Street", "Nguyen Hue Boulevard", "Dong Khoi Street",
            "Le Thanh Ton Street", "Hai Ba Trung Street", "Pasteur Street",
            "Ly Tu Trong Street", "Ton That Thiep Street", "Mac Dinh Chi Street",
            "Nguyen Thai Binh Street", "Tran Hung Dao Street", "Vo Van Tan Street"
    };

    private static final String[] WARDS = {
            "Ben Nghe Ward", "Ben Thanh Ward", "Nguyen Thai Binh Ward",
            "Da Kao Ward", "Tan Dinh Ward", "Vo Thi Sau Ward",
            "District 1", "District 2", "District 3"
    };

    private static final String[] CITIES = {
            "Ho Chi Minh City", "Hanoi", "Da Nang", "Can Tho", "Hai Phong"
    };

    private static final Random random = new Random();

    public static void main(String[] args) {
        // Create excel folder in current project directory
        String projectPath = System.getProperty("user.dir");
        String excelFolderPath = projectPath + File.separator + "excel_files";

        // Create folder if it doesn't exist
        File excelFolder = new File(excelFolderPath);
        if (!excelFolder.exists()) {
            excelFolder.mkdirs();
            System.out.println("Created folder: " + excelFolderPath);
        }

        // Generate 5000 records
        generateExcel(excelFolderPath + File.separator + "parking_lots_5000.xlsx", 5000);

        // Generate empty template with just 1 example row
        generateExcel(excelFolderPath + File.separator + "parking_lot_template.xlsx", 1);

        System.out.println("\n========================================");
        System.out.println("Excel files generated successfully!");
        System.out.println("Location: " + excelFolderPath);
        System.out.println("========================================");
    }

    public static void generateExcel(String filePath, int recordCount) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Parking Lots");

            // Create header row
            createHeaderRow(sheet, workbook);

            // Generate data rows
            for (int i = 1; i <= recordCount; i++) {
                createDataRow(sheet, i);
                if (i % 1000 == 0) {
                    System.out.println("Generated " + i + " records...");
                }
            }

            // Auto-size columns
            for (int i = 0; i < 17; i++) {
                sheet.autoSizeColumn(i);
            }

            // Write to file
            try (FileOutputStream fileOut = new FileOutputStream(filePath)) {
                workbook.write(fileOut);
            }

            System.out.println("✓ Generated: " + new File(filePath).getName() + " with " + recordCount + " records");

        } catch (Exception e) {
            System.err.println("✗ Error generating Excel: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void createHeaderRow(Sheet sheet, Workbook workbook) {
        Row headerRow = sheet.createRow(0);

        // Create header style
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

        String[] headers = {
                "Partner ID", "Name", "Street Address", "Ward", "City",
                "Latitude", "Longitude", "Total Floors",
                "Operating Hours Start", "Operating Hours End", "Is 24 Hour",
                "Boundary Top Left X", "Boundary Top Left Y",
                "Boundary Width", "Boundary Height", "Status", "Reason"
        };

        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
    }

    private static void createDataRow(Sheet sheet, int rowIndex) {
        Row row = sheet.createRow(rowIndex);

        // Partner ID (1-10)
        row.createCell(0).setCellValue(random.nextInt(10) + 1);

        // Name
        String name = PARKING_LOT_NAMES[random.nextInt(PARKING_LOT_NAMES.length)] + " " + rowIndex;
        row.createCell(1).setCellValue(name);

        // Street Address
        int streetNumber = random.nextInt(500) + 1;
        String streetAddress = streetNumber + " " + STREETS[random.nextInt(STREETS.length)];
        row.createCell(2).setCellValue(streetAddress);

        // Ward
        row.createCell(3).setCellValue(WARDS[random.nextInt(WARDS.length)]);

        // City
        row.createCell(4).setCellValue(CITIES[random.nextInt(CITIES.length)]);

        // Latitude (Ho Chi Minh City area: 10.7 - 10.9)
        double latitude = 10.7 + (random.nextDouble() * 0.2);
        row.createCell(5).setCellValue(Math.round(latitude * 1000000.0) / 1000000.0);

        // Longitude (Ho Chi Minh City area: 106.6 - 106.8)
        double longitude = 106.6 + (random.nextDouble() * 0.2);
        row.createCell(6).setCellValue(Math.round(longitude * 1000000.0) / 1000000.0);

        // Total Floors (1-10)
        row.createCell(7).setCellValue(random.nextInt(10) + 1);

        // Is 24 Hour
        boolean is24Hour = random.nextBoolean();
        row.createCell(10).setCellValue(is24Hour);

        // Operating Hours
        if (is24Hour) {
            row.createCell(8).setCellValue("00:00:00");
            row.createCell(9).setCellValue("00:00:00");
        } else {
            // Start time (6:00 - 8:00)
            int startHour = random.nextInt(3) + 6;
            row.createCell(8).setCellValue(String.format("%02d:00:00", startHour));

            // End time (20:00 - 23:00)
            int endHour = random.nextInt(4) + 20;
            row.createCell(9).setCellValue(String.format("%02d:00:00", endHour));
        }

        // Boundary coordinates
        row.createCell(11).setCellValue(0.0); // Boundary Top Left X
        row.createCell(12).setCellValue(0.0); // Boundary Top Left Y
        row.createCell(13).setCellValue(random.nextInt(200) + 50.0); // Width
        row.createCell(14).setCellValue(random.nextInt(200) + 50.0); // Height

        // Status (weighted towards ACTIVE)
        String status;
        int statusRand = random.nextInt(100);
        if (statusRand < 70) {
            status = "ACTIVE";
        } else if (statusRand < 80) {
            status = "UNDER_SURVEY";
        } else if (statusRand < 90) {
            status = "PREPARING";
        } else if (statusRand < 95) {
            status = "PARTNER_CONFIGURATION";
        } else if (statusRand < 97) {
            status = "REJECTED";
        } else {
            status = "MAP_DENIED";
        }
        row.createCell(15).setCellValue(status);

        // Reason (only for REJECTED or MAP_DENIED)
        if (status.equals("REJECTED") || status.equals("MAP_DENIED")) {
            String[] reasons = {
                    "Does not meet safety standards",
                    "Insufficient capacity",
                    "Location not suitable",
                    "Pricing rules not appropriate"
            };
            row.createCell(16).setCellValue(reasons[random.nextInt(reasons.length)]);
        } else {
            row.createCell(16).setCellValue("");
        }
    }
}