package com.parkmate.vehicle;

import com.parkmate.account.Account;
import com.parkmate.account.AccountRepository;
import com.parkmate.common.exception.AppException;
import com.parkmate.common.exception.ErrorCode;
import com.parkmate.common.util.PaginationUtil;
import com.parkmate.partner.dto.ImportError;
import com.parkmate.user.User;
import com.parkmate.user.UserRepository;
import com.parkmate.vehicle.dto.*;
import com.querydsl.core.types.Predicate;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class VehicleServiceImpl implements VehicleService {

    private final VehicleRepository vehicleRepository;
    private final VehicleMapper vehicleMapper;
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final Validator validator;

    private static final List<String> ALLOWED_EXTENSIONS = List.of("xlsx", "xls");
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB

    @Override
    public VehicleResponse findById(Long id) {
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.VEHICLE_NOT_FOUND, id));
        return vehicleMapper.toDTO(vehicle);
    }

    @Override
    public VehicleResponse findByLicensePlate(String licensePlate) {
        return null;
    }

    @Override
    public VehicleResponse createVehicle(CreateVehicleRequest request, String userId) {

        if (vehicleRepository.existsByLicensePlate(request.getLicensePlate())) {
            throw new AppException(ErrorCode.VEHICLE_ALREADY_EXISTS, request.getLicensePlate());
        }

        if (userId != null && !userId.isEmpty()) {
            Account account = accountRepository.findById(Long.parseLong(userId))
                    .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_FOUND, userId));
            request.setUserId(account.getUser().getId());
        }

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND, request.getUserId()));

        Vehicle vehicle = vehicleMapper.toEntity(request);

        vehicle.setUser(user);

        Vehicle savedVehicle = vehicleRepository.save(vehicle);

        return vehicleMapper.toDTO(savedVehicle);
    }

    @Override
    public VehicleResponse updateVehicle(Long id, UpdateVehicleRequest request) {

        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.VEHICLE_NOT_FOUND, id));

        vehicleMapper.updateEntityFromDTO(request, vehicle);

        Vehicle updatedVehicle = vehicleRepository.save(vehicle);
        return vehicleMapper.toDTO(updatedVehicle);
    }

    @Override
    public Page<VehicleResponse> findAll(int page,
                                         int size,
                                         String sortBy,
                                         String sortOrder,
                                         VehicleSearchCriteria searchCriteria,
                                         String accountIdHeader) {
        Long userId = null;
        // X-User-Id header contains accountId, need to convert to userId
        if (accountIdHeader != null && !accountIdHeader.isEmpty() && searchCriteria.isOwnedByMe()) {
            Account account = accountRepository.findById(Long.parseLong(accountIdHeader))
                    .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_FOUND, accountIdHeader));
            userId = account.getUser().getId();
        }

        System.out.println("DEBUG - accountId header: " + accountIdHeader);
        System.out.println("DEBUG - converted userId: " + userId);
        System.out.println("DEBUG - searchCriteria: " + searchCriteria);

        Predicate predicate = VehicleSpecification.buildPredicate(searchCriteria, userId);
        System.out.println("DEBUG - predicate: " + predicate);

        Pageable pageable = PaginationUtil.parsePageable(page, size, sortBy, sortOrder);
        Page<Vehicle> vehiclePage = vehicleRepository.findAll(predicate, pageable);
        System.out.println("DEBUG - total elements: " + vehiclePage.getTotalElements());

        return vehiclePage.map(vehicleMapper::toDTO);
    }

    @Override
    public void deleteVehicle(Long id) {
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.VEHICLE_NOT_FOUND, id));

        vehicle.setActive(false);
        vehicleRepository.save(vehicle);
    }

    @Override
    @Transactional
    public ImportVehicleResponse importVehiclesFromExcel(MultipartFile file) {
        ImportVehicleResponse response = new ImportVehicleResponse();

        // Validate file
        String validationError = validateFile(file);
        if (validationError != null) {
            response.addError(new ImportError(0, validationError));
            return response;
        }

        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);

            // Get existing license plates to check duplicate
            Set<String> existingLicensePlates = new HashSet<>(
                    vehicleRepository.findAll().stream().map(Vehicle::getLicensePlate).toList()
            );

            // Create user phone to user map for faster lookup
            Map<String, User> phoneToUserMap = new HashMap<>();
            userRepository.findAll().forEach(user -> phoneToUserMap.put(user.getPhone(), user));

            List<Vehicle> vehiclesToSave = new ArrayList<>();
            Set<String> licensePlatesInFile = new HashSet<>();

            int totalRows = sheet.getPhysicalNumberOfRows() - 1; // Minus header row
            response.setTotalRows(totalRows);

            // Loop through rows (skip row 0 - header)
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);

                if (isRowEmpty(row)) {
                    continue; // Skip empty rows
                }

                try {
                    // Parse row to DTO
                    ImportVehicleDTO dto = parseRowToVehicleDTO(row, i + 1);

                    // Validate DTO
                    Set<ConstraintViolation<ImportVehicleDTO>> violations = validator.validate(dto);

                    if (!violations.isEmpty()) {
                        for (ConstraintViolation<ImportVehicleDTO> violation : violations) {
                            response.addError(new ImportError(
                                    i + 1,
                                    violation.getPropertyPath().toString(),
                                    violation.getMessage()
                            ));
                        }
                        continue;
                    }

                    // Check duplicate license plate in DB
                    if (existingLicensePlates.contains(dto.getLicensePlate())) {
                        response.addError(new ImportError(
                                i + 1,
                                "licensePlate",
                                "License plate already exists in database: " + dto.getLicensePlate()
                        ));
                        continue;
                    }

                    // Check duplicate license plate in file
                    if (licensePlatesInFile.contains(dto.getLicensePlate())) {
                        response.addError(new ImportError(
                                i + 1,
                                "licensePlate",
                                "Duplicate license plate in file: " + dto.getLicensePlate()
                        ));
                        continue;
                    }

                    // Verify user exists
                    User user = phoneToUserMap.get(dto.getUserPhone());
                    if (user == null) {
                        response.addError(new ImportError(
                                i + 1,
                                "userPhone",
                                "User with phone not found: " + dto.getUserPhone()
                        ));
                        continue;
                    }

                    // Convert DTO to Entity
                    Vehicle vehicle = convertVehicleDTOToEntity(dto, user);
                    vehiclesToSave.add(vehicle);
                    licensePlatesInFile.add(dto.getLicensePlate());

                } catch (Exception e) {
                    log.error("Error processing row {}: {}", i + 1, e.getMessage());
                    response.addError(new ImportError(
                            i + 1,
                            "Error parsing row: " + e.getMessage()
                    ));
                }
            }

            // Batch save all valid vehicles
            if (!vehiclesToSave.isEmpty()) {
                vehicleRepository.saveAll(vehiclesToSave);
                response.setSuccessCount(vehiclesToSave.size());
                log.info("Successfully imported {} vehicles", vehiclesToSave.size());
            }

        } catch (IOException e) {
            log.error("Error reading Excel file: {}", e.getMessage());
            response.addError(new ImportError(0, "Error reading file: " + e.getMessage()));
        }

        return response;
    }

    @Override
    public long count() {
        return vehicleRepository.count();
    }

    @Override
    public void exportVehiclesToExcel(VehicleSearchCriteria searchCriteria, String accountIdHeader, java.io.OutputStream outputStream) throws java.io.IOException {
        Long userId = null;
        // X-User-Id header contains accountId, need to convert to userId
        if (accountIdHeader != null && !accountIdHeader.isEmpty() && searchCriteria != null && searchCriteria.isOwnedByMe()) {
            Account account = accountRepository.findById(Long.parseLong(accountIdHeader))
                    .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_FOUND, accountIdHeader));
            userId = account.getUser().getId();
        }

        // Get vehicles based on search criteria
        Predicate predicate = VehicleSpecification.buildPredicate(searchCriteria, userId);
        Iterable<Vehicle> vehiclesIterable = vehicleRepository.findAll(predicate);
        java.util.List<Vehicle> vehicles = new java.util.ArrayList<>();
        vehiclesIterable.forEach(vehicles::add);

        // Create workbook and sheet
        org.apache.poi.ss.usermodel.Workbook workbook = new org.apache.poi.xssf.usermodel.XSSFWorkbook();
        org.apache.poi.ss.usermodel.Sheet sheet = workbook.createSheet("Vehicles");

        // Create header row
        org.apache.poi.ss.usermodel.Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("License Plate");
        header.createCell(1).setCellValue("Vehicle Type");
        header.createCell(2).setCellValue("Vehicle Brand");
        header.createCell(3).setCellValue("Vehicle Model");
        header.createCell(4).setCellValue("Vehicle Color");
        header.createCell(5).setCellValue("Is Active");
        header.createCell(6).setCellValue("Is Electric");
        header.createCell(7).setCellValue("User Phone");
        header.createCell(8).setCellValue("User Full Name");
        header.createCell(9).setCellValue("Created At");
        header.createCell(10).setCellValue("Updated At");

        // Fill data rows
        for (int i = 0; i < vehicles.size(); i++) {
            Vehicle vehicle = vehicles.get(i);
            org.apache.poi.ss.usermodel.Row row = sheet.createRow(i + 1);

            row.createCell(0).setCellValue(vehicle.getLicensePlate());
            row.createCell(1).setCellValue(vehicle.getVehicleType() != null ? vehicle.getVehicleType().toString() : "");
            row.createCell(2).setCellValue(vehicle.getVehicleBrand());
            row.createCell(3).setCellValue(vehicle.getVehicleModel());
            row.createCell(4).setCellValue(vehicle.getVehicleColor());
            row.createCell(5).setCellValue(vehicle.isActive() ? "Yes" : "No");
            row.createCell(6).setCellValue(vehicle.isElectric() ? "Yes" : "No");
            row.createCell(7).setCellValue(vehicle.getUser() != null ? vehicle.getUser().getPhone() : "");
            row.createCell(8).setCellValue(vehicle.getUser() != null ? vehicle.getUser().getFullName() : "");
            row.createCell(9).setCellValue(vehicle.getCreatedAt() != null ? vehicle.getCreatedAt().toString() : "");
            row.createCell(10).setCellValue(vehicle.getUpdatedAt() != null ? vehicle.getUpdatedAt().toString() : "");
        }

        // Auto-size columns
        for (int i = 0; i < 11; i++) {
            sheet.autoSizeColumn(i);
        }

        // Write to output stream
        workbook.write(outputStream);
        workbook.close();
    }

    private String validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return "File is empty";
        }

        String filename = file.getOriginalFilename();
        if (filename == null) {
            return "Invalid filename";
        }

        String extension = filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            return "Invalid file type. Only .xlsx and .xls are allowed";
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            return "File size exceeds 5MB limit";
        }

        return null;
    }

    private boolean isRowEmpty(Row row) {
        if (row == null) {
            return true;
        }

        for (int i = 0; i < row.getLastCellNum(); i++) {
            Cell cell = row.getCell(i);
            if (cell != null && cell.getCellType() != CellType.BLANK) {
                return false;
            }
        }
        return true;
    }

    private ImportVehicleDTO parseRowToVehicleDTO(Row row, int rowNumber) {
        return ImportVehicleDTO.builder()
                .rowNumber(rowNumber)
                .licensePlate(getCellValueAsString(row.getCell(0)))
                .vehicleType(getCellValueAsString(row.getCell(1)))
                .userPhone(getCellValueAsString(row.getCell(2)))
                .vehicleBrand(getCellValueAsString(row.getCell(3)))
                .vehicleModel(getCellValueAsString(row.getCell(4)))
                .vehicleColor(getCellValueAsString(row.getCell(5)))
                .isActive(getCellValueAsString(row.getCell(6)))
                .isElectric(getCellValueAsString(row.getCell(7)))
                .build();
    }

    private String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return null;
        }

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getLocalDateTimeCellValue().toString();
                }
                // Convert number to string (remove decimal if it's whole number)
                double numValue = cell.getNumericCellValue();
                if (numValue == Math.floor(numValue)) {
                    return String.valueOf((long) numValue);
                }
                return String.valueOf(numValue);
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case BLANK:
            default:
                return null;
        }
    }

    private Vehicle convertVehicleDTOToEntity(ImportVehicleDTO dto, User user) {
        VehicleType vehicleType;
        try {
            vehicleType = VehicleType.valueOf(dto.getVehicleType().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new AppException(ErrorCode.INVALID_VEHICLE_TYPE, dto.getVehicleType());
        }

        return Vehicle.builder()
                .licensePlate(dto.getLicensePlate())
                .vehicleType(vehicleType)
                .user(user)
                .vehicleBrand(dto.getVehicleBrand())
                .vehicleModel(dto.getVehicleModel())
                .vehicleColor(dto.getVehicleColor())
                .isActive(parseBoolean(dto.getIsActive(), true))
                .isElectric(parseBoolean(dto.getIsElectric(), false))
                .build();
    }

    private boolean parseBoolean(String value, boolean defaultValue) {
        if (value == null || value.isEmpty()) {
            return defaultValue;
        }
        return value.equalsIgnoreCase("true") || value.equalsIgnoreCase("yes") || value.equals("1");
    }


}
