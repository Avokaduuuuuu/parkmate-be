

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Random;

public class ExcelGenerator {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public static void main(String[] args) {
        try {
            // Generate Users Excel
            // generateUsersExcel("users_5000.xlsx", 5000);

            // Generate Vehicles Excel for user with phone 0910000001
            generateVehiclesExcel("vehicles_5000.xlsx", 5000, "1241413123");

            System.out.println("Excel file generated successfully!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void generateUsersExcel(String filename, int rows) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Users");
        Random random = new Random();

        // Header
        Row headerRow = sheet.createRow(0);
        String[] headers = {
                "Phone", "First Name", "Last Name", "Full Name",
                "Date of Birth", "Address", "ID Number", "Issue Place",
                "Issue Date", "Expiry Date"
        };

        CellStyle headerStyle = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        headerStyle.setFont(font);

        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        // Vietnamese first names and last names
        String[] firstNames = {"Nguyen", "Tran", "Le", "Pham", "Hoang", "Phan", "Vu", "Dang", "Bui", "Do"};
        String[] middleNames = {"Van", "Thi", "Minh", "Hoang", "Anh", "Duc", "Thanh", "Quoc", "Ngoc", "Hong"};
        String[] lastNames = {"An", "Binh", "Cuong", "Dung", "Hai", "Hung", "Linh", "Mai", "Nam", "Phuong", "Quan", "Son", "Tam", "Thinh", "Tuan", "Vy", "Xuan", "Yen"};
        String[] cities = {"Ho Chi Minh City", "Hanoi", "Da Nang", "Can Tho", "Hai Phong", "Bien Hoa", "Vung Tau", "Nha Trang", "Hue"};
        String[] districts = {"District 1", "District 2", "District 3", "District 5", "District 7", "Binh Thanh", "Phu Nhuan", "Tan Binh", "Go Vap"};
        String[] streets = {"Nguyen Hue", "Le Loi", "Tran Hung Dao", "Vo Thi Sau", "Hai Ba Trung", "Ly Thuong Kiet", "Nguyen Trai", "Phan Chu Trinh"};

        // Data rows
        for (int i = 1; i <= rows; i++) {
            Row row = sheet.createRow(i);

            // Phone (unique, 10 digits starting with 09 or 08 or 07)
            String phonePrefix = new String[]{"09", "08", "07"}[random.nextInt(3)];
            String phone = phonePrefix + String.format("%08d", 10000000 + i);
            row.createCell(0).setCellValue(phone);

            // Names
            String firstName = firstNames[random.nextInt(firstNames.length)];
            String middleName = middleNames[random.nextInt(middleNames.length)];
            String lastName = lastNames[random.nextInt(lastNames.length)];
            String fullName = firstName + " " + middleName + " " + lastName;

            row.createCell(1).setCellValue(firstName);
            row.createCell(2).setCellValue(middleName + " " + lastName);
            row.createCell(3).setCellValue(fullName);

            // Date of Birth (between 1970 and 2005)
            LocalDate dob = LocalDate.of(1970 + random.nextInt(35), random.nextInt(12) + 1, random.nextInt(28) + 1);
            row.createCell(4).setCellValue(dob.format(DATE_FORMATTER));

            // Address
            String address = (random.nextInt(999) + 1) + " " +
                           streets[random.nextInt(streets.length)] + " Street, " +
                           districts[random.nextInt(districts.length)] + ", " +
                           cities[random.nextInt(cities.length)];
            row.createCell(5).setCellValue(address);

            // ID Number (12 digits)
            String idNumber = String.format("%012d", 100000000000L + i);
            row.createCell(6).setCellValue(idNumber);

            // Issue Place
            String issuePlace = cities[random.nextInt(cities.length)] + " Police Department";
            row.createCell(7).setCellValue(issuePlace);

            // Issue Date (between 2010 and 2024)
            LocalDate issueDate = LocalDate.of(2010 + random.nextInt(15), random.nextInt(12) + 1, random.nextInt(28) + 1);
            row.createCell(8).setCellValue(issueDate.format(DATE_FORMATTER));

            // Expiry Date (15 years after issue date for people under 40, no expiry for others)
            int age = LocalDate.now().getYear() - dob.getYear();
            if (age < 40) {
                LocalDate expiryDate = issueDate.plusYears(15);
                row.createCell(9).setCellValue(expiryDate.format(DATE_FORMATTER));
            } else {
                row.createCell(9).setCellValue(""); // No expiry
            }

            if (i % 100 == 0) {
                System.out.println("Generated " + i + " rows");
            }
        }

        // Auto-size
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }

        // Write file
        try (FileOutputStream out = new FileOutputStream(filename)) {
            workbook.write(out);
        }

        workbook.close();
    }

    public static void generateVehiclesExcel(String filename, int rows, String userPhone) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Vehicles");
        Random random = new Random();

        // Header
        Row headerRow = sheet.createRow(0);
        String[] headers = {
                "License Plate", "Vehicle Type", "User Phone",
                "Vehicle Brand", "Vehicle Model", "Vehicle Color",
                "Is Active", "Is Electric"
        };

        CellStyle headerStyle = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        headerStyle.setFont(font);

        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        // Vehicle types
        String[] vehicleTypes = {"CAR_UP_TO_9_SEATS", "MOTORBIKE"};

        // Car brands and models
        String[] carBrands = {"Toyota", "Honda", "Mazda", "Hyundai", "Ford", "Kia", "Mitsubishi", "Nissan", "Suzuki", "Chevrolet"};
        String[][] carModels = {
                {"Vios", "Camry", "Corolla", "Fortuner", "Innova", "Veloz"},
                {"City", "Civic", "Accord", "CR-V", "HR-V", "BR-V"},
                {"3", "6", "CX-5", "CX-8", "CX-3"},
                {"Accent", "Elantra", "Tucson", "Santa Fe", "Grand i10"},
                {"Ranger", "Everest", "Explorer", "Focus", "EcoSport"},
                {"Morning", "Cerato", "K3", "Seltos", "Sorento"},
                {"Xpander", "Outlander", "Pajero Sport", "Attrage", "Triton"},
                {"Navara", "Terra", "X-Trail", "Sunny", "Almera"},
                {"Swift", "Ertiga", "Ciaz", "XL7", "Vitara"},
                {"Colorado", "Trailblazer", "Captiva", "Spark", "Cruze"}
        };

        // Motorbike brands and models
        String[] motorbikeBrands = {"Honda", "Yamaha", "Suzuki", "SYM", "Piaggio", "Vespa"};
        String[][] motorbikeModels = {
                {"Wave", "Vision", "Air Blade", "SH", "Winner X", "Future"},
                {"Exciter", "Sirius", "Janus", "Grande", "FreeGo"},
                {"Raider", "Satria", "GSX", "Address"},
                {"Galaxy", "Attila", "Elite", "Husky"},
                {"Liberty", "Medley", "Zip"},
                {"Sprint", "Primavera", "GTS"}
        };

        // Colors
        String[] colors = {"White", "Black", "Silver", "Red", "Blue", "Gray", "Brown", "Green"};

        // City codes for Vietnam license plates
        String[] cityCodes = {"30", "51", "43", "29", "50", "59", "72", "79", "92", "99"}; // HCM, Hanoi, Da Nang, etc.

        // Data rows
        for (int i = 1; i <= rows; i++) {
            Row row = sheet.createRow(i);

            // Vehicle Type
            String vehicleType = vehicleTypes[random.nextInt(vehicleTypes.length)];
            boolean isCar = vehicleType.equals("CAR");

            // License Plate - Format: 51A-123.45 (Vietnam format)
            String cityCode = cityCodes[random.nextInt(cityCodes.length)];
            String letter = String.valueOf((char) ('A' + random.nextInt(26)));
            String numbers = String.format("%03d.%02d", random.nextInt(1000), random.nextInt(100));
            String licensePlate = cityCode + letter + "-" + numbers;
            row.createCell(0).setCellValue(licensePlate);

            // Vehicle Type
            row.createCell(1).setCellValue(vehicleType);

            // User Phone - Use provided phone number
            row.createCell(2).setCellValue(userPhone);

            // Brand and Model
            String brand, model;
            if (isCar) {
                int brandIndex = random.nextInt(carBrands.length);
                brand = carBrands[brandIndex];
                model = carModels[brandIndex][random.nextInt(carModels[brandIndex].length)];
            } else {
                int brandIndex = random.nextInt(motorbikeBrands.length);
                brand = motorbikeBrands[brandIndex];
                model = motorbikeModels[brandIndex][random.nextInt(motorbikeModels[brandIndex].length)];
            }

            row.createCell(3).setCellValue(brand);
            row.createCell(4).setCellValue(model);

            // Color
            String color = colors[random.nextInt(colors.length)];
            row.createCell(5).setCellValue(color);

            // Is Active (90% true, 10% false)
            boolean isActive = random.nextInt(100) < 90;
            row.createCell(6).setCellValue(isActive ? "Yes" : "No");

            // Is Electric (10% true for cars, 5% for motorbikes)
            boolean isElectric = isCar ? random.nextInt(100) < 10 : random.nextInt(100) < 5;
            row.createCell(7).setCellValue(isElectric ? "Yes" : "No");

            if (i % 100 == 0) {
                System.out.println("Generated " + i + " rows");
            }
        }

        // Auto-size
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }

        // Write file
        try (FileOutputStream out = new FileOutputStream(filename)) {
            workbook.write(out);
        }

        workbook.close();
    }
}