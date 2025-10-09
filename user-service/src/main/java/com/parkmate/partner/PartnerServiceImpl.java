package com.parkmate.partner;

import com.parkmate.common.exception.AppException;
import com.parkmate.common.exception.ErrorCode;
import com.parkmate.common.util.PaginationUtil;
import com.parkmate.partner.dto.*;
import com.parkmate.s3.S3Service;
import com.querydsl.core.types.Predicate;
import jakarta.transaction.Transactional;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RequiredArgsConstructor
@Service
@Slf4j
public class PartnerServiceImpl implements PartnerService {


    private final PartnerRepository partnerRepository;
    private final PartnerMapper partnerMapper;
    private final S3Service s3Service;

    private final Validator validator;

    private static final List<String> ALLOWED_EXTENSIONS = List.of("xlsx", "xls");
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB


    @Override
    public Page<PartnerResponse> search(PartnerSearchCriteria criteria, int page, int size, String sortBy, String sortOrder) {
        Predicate predicate = PartnerSpecification.buildPredicate(criteria);
        Pageable pageable = PaginationUtil.parsePageable(page, size, sortBy, sortOrder);
        return partnerRepository.findAll(predicate, pageable).map(partnerMapper::toDto);
    }

    @Override
    public List<PartnerResponse> search(PartnerSearchCriteria criteria) {
        Predicate predicate = PartnerSpecification.buildPredicate(criteria);
        Iterable<Partner> partners = partnerRepository.findAll(predicate);
        List<Partner> partnerList = new ArrayList<>();
        partners.forEach(partnerList::add);
        return partnerList.stream().map(partnerMapper::toDto).toList();
    }

    @Override
    public PartnerResponse create(CreatePartnerRequest request) {

        Partner partner = partnerMapper.toEntity(request);
        partner = partnerRepository.save(partner);
        return partnerMapper.toDto(partner);

    }

    @Override
    public PartnerResponse update(long id, UpdatePartnerRequest request) {

        Partner partner = partnerRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.PARTNER_NOT_FOUND, "Partner not found"));
        partnerMapper.updateEntityFromDto(request, partner);
        partner = partnerRepository.save(partner);
        return partnerMapper.toDto(partner);
    }

    @Override
    public PartnerResponse findById(long id) {
        Partner partner = partnerRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.PARTNER_NOT_FOUND, "Partner not found"));
        String presignedUrl = s3Service.generatePresignedUrl(partner.getBusinessLicenseFileUrl());
        PartnerResponse response = partnerMapper.toDto(partner);
        return new PartnerResponse(
                response.id(),
                response.companyName(),
                response.taxNumber(),
                response.businessLicenseNumber(),
                presignedUrl,
                response.companyAddress(),
                response.companyPhone(),
                response.companyEmail(),
                response.businessDescription(),
                response.status(),
                response.suspensionReason(),
                response.createdAt(),
                response.updatedAt(),
                response.accounts()
        );
    }

    @Override
    public void delete(long id) {
        Partner partner = partnerRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.PARTNER_NOT_FOUND, "Partner not found"));
        partner.setStatus(PartnerStatus.DELETED);
        partnerRepository.delete(partner);
    }


    @Transactional
    @Override
    public ImportPartnerResponse importPartnersFromExcel(MultipartFile file) {
        ImportPartnerResponse response = new ImportPartnerResponse();

        // Validate file
        String validationError = validateFile(file);
        if (validationError != null) {
            response.addError(new ImportError(0, validationError));
            return response;
        }

        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);

            // Get existing tax numbers để check duplicate
            Set<String> existingTaxNumbers = new HashSet<>(
                    partnerRepository.findAllTaxNumbers()
            );

            List<Partner> partnersToSave = new ArrayList<>();
            Set<String> taxNumbersInFile = new HashSet<>();

            int totalRows = sheet.getPhysicalNumberOfRows() - 1; // Trừ header row
            response.setTotalRows(totalRows);

            // Loop qua các rows (bỏ qua row 0 - header)
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);

                if (isRowEmpty(row)) {
                    continue; // Skip empty rows
                }

                try {
                    // Parse row to DTO
                    ImportPartnerDTO dto = parseRowToDTO(row, i + 1);

                    // Validate DTO
                    Set<ConstraintViolation<ImportPartnerDTO>> violations =
                            validator.validate(dto);

                    if (!violations.isEmpty()) {
                        for (ConstraintViolation<ImportPartnerDTO> violation : violations) {
                            response.addError(new ImportError(
                                    i + 1,
                                    violation.getPropertyPath().toString(),
                                    violation.getMessage()
                            ));
                        }
                        continue;
                    }

                    // Check duplicate tax number trong DB
                    if (existingTaxNumbers.contains(dto.getTaxNumber())) {
                        response.addError(new ImportError(
                                i + 1,
                                "taxNumber",
                                "Tax number already exists in database: " + dto.getTaxNumber()
                        ));
                        continue;
                    }

                    // Check duplicate tax number trong file Excel
                    if (taxNumbersInFile.contains(dto.getTaxNumber())) {
                        response.addError(new ImportError(
                                i + 1,
                                "taxNumber",
                                "Duplicate tax number in file: " + dto.getTaxNumber()
                        ));
                        continue;
                    }

                    // Convert DTO to Entity
                    Partner partner = convertToEntity(dto);
                    partnersToSave.add(partner);
                    taxNumbersInFile.add(dto.getTaxNumber());

                } catch (Exception e) {
                    log.error("Error processing row {}: {}", i + 1, e.getMessage());
                    response.addError(new ImportError(
                            i + 1,
                            "Error parsing row: " + e.getMessage()
                    ));
                }
            }

            // Batch save all valid partners
            if (!partnersToSave.isEmpty()) {
                partnerRepository.saveAll(partnersToSave);
                response.setSuccessCount(partnersToSave.size());
                log.info("Successfully imported {} partners", partnersToSave.size());
            }

        } catch (IOException e) {
            log.error("Error reading Excel file: {}", e.getMessage());
            response.addError(new ImportError(0, "Error reading file: " + e.getMessage()));
        }

        return response;
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

    private ImportPartnerDTO parseRowToDTO(Row row, int rowNumber) {
        return ImportPartnerDTO.builder()
                .rowNumber(rowNumber)
                .companyName(getCellValueAsString(row.getCell(0)))
                .taxNumber(getCellValueAsString(row.getCell(1)))
                .businessLicenseNumber(getCellValueAsString(row.getCell(2)))
                .businessLicenseFileUrl(getCellValueAsString(row.getCell(3)))
                .companyAddress(getCellValueAsString(row.getCell(4)))
                .companyPhone(getCellValueAsString(row.getCell(5)))
                .companyEmail(getCellValueAsString(row.getCell(6)))
                .businessDescription(getCellValueAsString(row.getCell(7)))
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
            case FORMULA:
                return getCellValueAsString(cell.getCellType());
            case BLANK:
            default:
                return null;
        }
    }

    private String getCellValueAsString(CellType cellType) {
        return null; // Handle formula evaluated value if needed
    }

    private Partner convertToEntity(ImportPartnerDTO dto) {
        return Partner.builder()
                .companyName(dto.getCompanyName())
                .taxNumber(dto.getTaxNumber())
                .businessLicenseNumber(dto.getBusinessLicenseNumber())
                .businessLicenseFileUrl(dto.getBusinessLicenseFileUrl())
                .companyAddress(dto.getCompanyAddress())
                .companyPhone(dto.getCompanyPhone())
                .companyEmail(dto.getCompanyEmail())
                .businessDescription(dto.getBusinessDescription())
                .status(PartnerStatus.APPROVED)
                .build();
    }

    @Override
    public long count() {
        return partnerRepository.count();
    }

    @Override
    public void exportPartnersToExcel(PartnerSearchCriteria criteria, java.io.OutputStream outputStream) throws java.io.IOException {
        // Get partners based on search criteria
        List<PartnerResponse> partners = search(criteria);

        // Create workbook and sheet
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Partners");

        // Create header row
        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("Company Name");
        header.createCell(1).setCellValue("Tax Number");
        header.createCell(2).setCellValue("Business License Number");
        header.createCell(3).setCellValue("Business License File URL");
        header.createCell(4).setCellValue("Company Address");
        header.createCell(5).setCellValue("Company Phone");
        header.createCell(6).setCellValue("Company Email");
        header.createCell(7).setCellValue("Business Description");
        header.createCell(8).setCellValue("Status");
        header.createCell(9).setCellValue("Suspension Reason");
        header.createCell(10).setCellValue("Created At");
        header.createCell(11).setCellValue("Updated At");

        // Fill data rows
        for (int i = 0; i < partners.size(); i++) {
            PartnerResponse partner = partners.get(i);
            Row row = sheet.createRow(i + 1);

            row.createCell(0).setCellValue(partner.companyName());
            row.createCell(1).setCellValue(partner.taxNumber());
            row.createCell(2).setCellValue(partner.businessLicenseNumber());
            row.createCell(3).setCellValue(partner.businessLicenseFileUrl());
            row.createCell(4).setCellValue(partner.companyAddress());
            row.createCell(5).setCellValue(partner.companyPhone());
            row.createCell(6).setCellValue(partner.companyEmail());
            row.createCell(7).setCellValue(partner.businessDescription());
            row.createCell(8).setCellValue(partner.status() != null ? partner.status().toString() : "");
            row.createCell(9).setCellValue(partner.suspensionReason());
            row.createCell(10).setCellValue(partner.createdAt() != null ? partner.createdAt().toString() : "");
            row.createCell(11).setCellValue(partner.updatedAt() != null ? partner.updatedAt().toString() : "");
        }

        // Auto-size columns
        for (int i = 0; i < 12; i++) {
            sheet.autoSizeColumn(i);
        }

        // Write to output stream
        workbook.write(outputStream);
        workbook.close();
    }


}
