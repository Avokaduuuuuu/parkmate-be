package com.parkmate.user;

import com.parkmate.account.Account;
import com.parkmate.account.AccountRepository;
import com.parkmate.client.PaymentClient;
import com.parkmate.client.dto.request.CreateWalletRequest;
import com.parkmate.common.enums.AccountRole;
import com.parkmate.common.enums.AccountStatus;
import com.parkmate.common.exception.AppException;
import com.parkmate.common.exception.ErrorCode;
import com.parkmate.common.util.PaginationUtil;
import com.parkmate.partner.dto.ImportError;
import com.parkmate.s3.S3Service;
import com.parkmate.user.dto.*;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.parkmate.auth.AuthServiceImpl.getUserResponse;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final S3Service s3Service;
    private final AccountRepository accountRepository;
    private final PaymentClient paymentClient;
    private final Validator validator;
    private final PasswordEncoder passwordEncoder;

    private static final List<String> ALLOWED_EXTENSIONS = List.of("xlsx", "xls");
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        return responseWithPresignedURL(userMapper.toResponse(user), user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(user -> responseWithPresignedURL(userMapper.toResponse(user), user))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getCurrentUser(String userIdHeader) {
        long accountId;

        // Try to get userId from header first (from gateway)
        if (userIdHeader != null && !userIdHeader.isEmpty()) {
            accountId = Long.parseLong(userIdHeader);
        } else {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        log.info("Fetching profile for user ID: {}", accountId);
        ;
        return getUserById(accountRepository.findById(accountId)
                .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_FOUND))
                .getUser()
                .getId());
    }

    @Override
    public Page<UserResponse> getAllUsers(int page, int size, String sortBy, String sortOrder, UserSearchCriteria criteria) {
        Pageable pageable = PaginationUtil.parsePageable(page, size, sortBy, sortOrder);
        return userRepository.findAll(UserSpecification.buildPredicate(criteria), pageable)
                .map(user -> responseWithPresignedURL(userMapper.toResponse(user), user));
    }

    @Override
    public UserResponse updateUser(Long id, UpdateUserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND, id));
        userMapper.updateEntity(request, user);
        createWalletIfNotExists(user);
        return responseWithPresignedURL(userMapper.toResponse(userRepository.save(user)), user);
    }

    private UserResponse responseWithPresignedURL(UserResponse response, User user) {
        return getUserResponse(response, user, s3Service);
    }

    private boolean hasWallet(User user) {
        return user.getIdNumber() == null && user.getIssueDate() == null && user.getExpiryDate() == null;
    }

    private void createWalletIfNotExists(User user) {
        if (!hasWallet(user)) {
            // Call wallet service to create wallet
            // walletService.createWallet(user.getId());
            CreateWalletRequest createWalletRequest = CreateWalletRequest.builder()
                    .userId(user.getId())
                    .build();
            paymentClient.createPayment(createWalletRequest);
            log.info("Creating wallet for user ID: {}", user.getId());
        }
    }

    @Override
    public ImportUserResponse importUsersFromExcel(MultipartFile file) {
        ImportUserResponse response = new ImportUserResponse();

        // Validate file
        String validationError = validateFile(file);
        if (validationError != null) {
            response.addError(new ImportError(0, validationError));
            return response;
        }

        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);

            // Get existing phones to check duplicate (more efficient query)
            Set<String> existingPhones = new HashSet<>(
                    userRepository.findAll().stream().map(User::getPhone).toList()
            );
            Set<String> existingAccountPhones = new HashSet<>(
                    accountRepository.findAll().stream().map(Account::getPhone).toList()
            );

            List<ImportUserDTO> validDTOs = new ArrayList<>();
            Set<String> phonesInFile = new HashSet<>();

            int totalRows = sheet.getPhysicalNumberOfRows() - 1; // Minus header row
            response.setTotalRows(totalRows);

            // PHASE 1: Parse and validate all rows (NO DATABASE CALLS)
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);

                if (isRowEmpty(row)) {
                    continue;
                }

                try {
                    ImportUserDTO dto = parseRowToUserDTO(row, i + 1);

                    // Validate DTO
                    Set<ConstraintViolation<ImportUserDTO>> violations = validator.validate(dto);
                    if (!violations.isEmpty()) {
                        for (ConstraintViolation<ImportUserDTO> violation : violations) {
                            response.addError(new ImportError(
                                    i + 1,
                                    violation.getPropertyPath().toString(),
                                    violation.getMessage()
                            ));
                        }
                        continue;
                    }

                    // Check duplicate phone in DB
                    if (existingPhones.contains(dto.getPhone()) || existingAccountPhones.contains(dto.getPhone())) {
                        response.addError(new ImportError(
                                i + 1,
                                "phone",
                                "Phone already exists in database: " + dto.getPhone()
                        ));
                        continue;
                    }

                    // Check duplicate phone in file
                    if (phonesInFile.contains(dto.getPhone())) {
                        response.addError(new ImportError(
                                i + 1,
                                "phone",
                                "Duplicate phone in file: " + dto.getPhone()
                        ));
                        continue;
                    }

                    validDTOs.add(dto);
                    phonesInFile.add(dto.getPhone());

                } catch (Exception e) {
                    log.error("Error processing row {}: {}", i + 1, e.getMessage());
                    response.addError(new ImportError(
                            i + 1,
                            "Error parsing row: " + e.getMessage()
                    ));
                }
            }

            // PHASE 2: Batch save in chunks (OPTIMIZED DATABASE CALLS)
            int batchSize = 100;
            int successCount = 0;

            for (int i = 0; i < validDTOs.size(); i += batchSize) {
                int end = Math.min(i + batchSize, validDTOs.size());
                List<ImportUserDTO> batch = validDTOs.subList(i, end);

                try {
                    int saved = saveBatchUsersWithAccounts(batch);
                    successCount += saved;
                    log.info("Saved batch {}-{}: {} users", i + 1, end, saved);
                } catch (Exception e) {
                    log.error("Error saving batch {}-{}: {}", i + 1, end, e.getMessage());
                    // Try to save individually for this failed batch
                    for (ImportUserDTO dto : batch) {
                        try {
                            saveUserWithAccount(dto);
                            successCount++;
                        } catch (Exception ex) {
                            response.addError(new ImportError(
                                    dto.getRowNumber(),
                                    "Error saving user: " + ex.getMessage()
                            ));
                        }
                    }
                }
            }

            response.setSuccessCount(successCount);
            log.info("Successfully imported {} users", successCount);

        } catch (IOException e) {
            log.error("Error reading Excel file: {}", e.getMessage());
            response.addError(new ImportError(0, "Error reading file: " + e.getMessage()));
        }

        return response;
    }

    @Transactional
    protected int saveBatchUsersWithAccounts(List<ImportUserDTO> dtos) {
        List<Account> accounts = new ArrayList<>();
        List<User> users = new ArrayList<>();

        // Create all accounts
        for (ImportUserDTO dto : dtos) {
            String email = dto.getPhone() + "@parkmate.system";
            Account account = Account.builder()
                    .email(email)
                    .phone(dto.getPhone())
                    .password(passwordEncoder.encode("ParkMate@123"))
                    .role(AccountRole.MEMBER)
                    .status(AccountStatus.ACTIVE)
                    .emailVerified(false)
                    .phoneVerified(true)
                    .build();
            accounts.add(account);
        }

        // Batch save accounts
        List<Account> savedAccounts = accountRepository.saveAll(accounts);

        // Create users with saved accounts
        for (int i = 0; i < dtos.size(); i++) {
            User user = convertUserDTOToEntity(dtos.get(i));
            user.setAccount(savedAccounts.get(i));
            users.add(user);
        }

        // Batch save users
        userRepository.saveAll(users);

        return users.size();
    }

    @Transactional
    protected void saveUserWithAccount(ImportUserDTO dto) {
        // Create Account first
        Account account = createAccountForUser(dto.getPhone());

        // Convert DTO to Entity with Account
        User user = convertUserDTOToEntity(dto);
        user.setAccount(account);

        // Save user
        userRepository.save(user);
    }

    @Override
    public long count() {
        return userRepository.count();
    }

    @Override
    public void exportUsersToExcel(UserSearchCriteria criteria, java.io.OutputStream outputStream) throws java.io.IOException {
        // Get users based on search criteria
        List<User> users;
        if (criteria != null) {
            users = (List<User>) userRepository.findAll(UserSpecification.buildPredicate(criteria));
        } else {
            users = userRepository.findAll();
        }

        // Create workbook and sheet
        org.apache.poi.ss.usermodel.Workbook workbook = new org.apache.poi.xssf.usermodel.XSSFWorkbook();
        org.apache.poi.ss.usermodel.Sheet sheet = workbook.createSheet("Users");

        // Create header row
        org.apache.poi.ss.usermodel.Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("Phone");
        header.createCell(1).setCellValue("First Name");
        header.createCell(2).setCellValue("Last Name");
        header.createCell(3).setCellValue("Full Name");
        header.createCell(4).setCellValue("Date of Birth");
        header.createCell(5).setCellValue("Address");
        header.createCell(6).setCellValue("ID Number");
        header.createCell(7).setCellValue("Issue Place");
        header.createCell(8).setCellValue("Issue Date");
        header.createCell(9).setCellValue("Expiry Date");
        header.createCell(10).setCellValue("Created At");
        header.createCell(11).setCellValue("Updated At");

        // Fill data rows
        for (int i = 0; i < users.size(); i++) {
            User user = users.get(i);
            org.apache.poi.ss.usermodel.Row row = sheet.createRow(i + 1);

            row.createCell(0).setCellValue(user.getPhone());
            row.createCell(1).setCellValue(user.getFirstName());
            row.createCell(2).setCellValue(user.getLastName());
            row.createCell(3).setCellValue(user.getFullName());
            row.createCell(4).setCellValue(user.getDateOfBirth() != null ? user.getDateOfBirth().toString() : "");
            row.createCell(5).setCellValue(user.getAddress());
            row.createCell(6).setCellValue(user.getIdNumber());
            row.createCell(7).setCellValue(user.getIssuePlace());
            row.createCell(8).setCellValue(user.getIssueDate() != null ? user.getIssueDate().toString() : "");
            row.createCell(9).setCellValue(user.getExpiryDate() != null ? user.getExpiryDate().toString() : "");
            row.createCell(10).setCellValue(user.getCreatedAt() != null ? user.getCreatedAt().toString() : "");
            row.createCell(11).setCellValue(user.getUpdatedAt() != null ? user.getUpdatedAt().toString() : "");
        }

        // Auto-size columns
        for (int i = 0; i < 12; i++) {
            sheet.autoSizeColumn(i);
        }

        // Write to output stream
        workbook.write(outputStream);
        workbook.close();
    }

    @Override
    public void deleteUser(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND, id));
        user.getAccount().setStatus(AccountStatus.DELETED);
        accountRepository.save(user.getAccount());
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

    private ImportUserDTO parseRowToUserDTO(Row row, int rowNumber) {
        return ImportUserDTO.builder()
                .rowNumber(rowNumber)
                .phone(getCellValueAsString(row.getCell(0)))
                .firstName(getCellValueAsString(row.getCell(1)))
                .lastName(getCellValueAsString(row.getCell(2)))
                .fullName(getCellValueAsString(row.getCell(3)))
                .dateOfBirth(getCellValueAsString(row.getCell(4)))
                .address(getCellValueAsString(row.getCell(5)))
                .idNumber(getCellValueAsString(row.getCell(6)))
                .issuePlace(getCellValueAsString(row.getCell(7)))
                .issueDate(getCellValueAsString(row.getCell(8)))
                .expiryDate(getCellValueAsString(row.getCell(9)))
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
                    return cell.getLocalDateTimeCellValue().toLocalDate().toString();
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

    private User convertUserDTOToEntity(ImportUserDTO dto) {
        User user = User.builder()
                .phone(dto.getPhone())
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .fullName(dto.getFullName())
                .address(dto.getAddress())
                .idNumber(dto.getIdNumber())
                .issuePlace(dto.getIssuePlace())
                .build();

        // Parse dates
        if (dto.getDateOfBirth() != null && !dto.getDateOfBirth().isEmpty()) {
            try {
                user.setDateOfBirth(LocalDate.parse(dto.getDateOfBirth(), DATE_FORMATTER));
            } catch (DateTimeParseException e) {
                log.warn("Invalid date format for dateOfBirth: {}", dto.getDateOfBirth());
            }
        }

        if (dto.getIssueDate() != null && !dto.getIssueDate().isEmpty()) {
            try {
                user.setIssueDate(LocalDate.parse(dto.getIssueDate(), DATE_FORMATTER));
            } catch (DateTimeParseException e) {
                log.warn("Invalid date format for issueDate: {}", dto.getIssueDate());
            }
        }

        if (dto.getExpiryDate() != null && !dto.getExpiryDate().isEmpty()) {
            try {
                user.setExpiryDate(LocalDate.parse(dto.getExpiryDate(), DATE_FORMATTER));
            } catch (DateTimeParseException e) {
                log.warn("Invalid date format for expiryDate: {}", dto.getExpiryDate());
            }
        }

        return user;
    }

    private Account createAccountForUser(String phone) {
        // Create account with phone and default password
        // Generate email from phone: phone@parkmate.system
        String email = phone + "@parkmate.system";

        Account account = Account.builder()
                .email(email)
                .phone(phone)
                .password(passwordEncoder.encode("ParkMate@123")) // Default password
                .role(AccountRole.MEMBER)
                .status(AccountStatus.ACTIVE)
                .emailVerified(false)
                .phoneVerified(true) // Assume phone is verified since we're importing
                .build();

        return accountRepository.save(account);
    }
}
