package com.parkmate.parking_lot;

import com.parkmate.exception.ErrorCode;
import com.parkmate.parking_lot.dto.req.ParkingLotCreateRequest;
import com.parkmate.parking_lot.dto.req.ParkingLotUpdateRequest;
import com.parkmate.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/api/v1/parking-service/lots")
@RequiredArgsConstructor
@Tag(name = "Parking Lot API", description = "API requests for Parking Lot")
public class ParkingLotController {

    private final ParkingLotService parkingLotService;
    private final ParkingLotImportService importService;
    private final ParkingLotExportService exportService;

    @GetMapping
    @Operation(
            summary = "Get all parking lots with filtering and pagination",
            description = """
                Search and retrieve parking lots with filtering and pagination support.
                
                **Query Parameters:**
                - `ownedByMe` (optional): Returns parking lots owned by current authenticated partner
                - `name` (optional): Search by parking lot name (partial match, case-insensitive)
                - `city` (optional): Search by city name (partial match, case-insensitive)
                - `is24Hour` (optional): Filter by 24-hour operation (true/false)
                - `status` (optional): Filter by parking lot status
                  * PENDING - Initial state, just created
                  * UNDER_SURVEY - Being surveyed and evaluated
                  * PREPARING - Infrastructure preparation phase
                  * PARTNER_CONFIGURATION - Partner configuring operations
                  * ACTIVE_PENDING - Awaiting admin approval
                  * ACTIVE - Fully operational
                  * INACTIVE - Temporarily closed
                  * UNDER_MAINTENANCE - Under repairs
                  * MAP_DENIED - Partner denied the created Map
                  * REJECTED - Application rejected
                  * DENIED - Access denied
                - `page` (optional): Page number (default: 0)
                - `size` (optional): Page size (default: 10)
                - `sortBy` (optional): Sort field (default: id) - Available: id, name, city, totalFloors, status, createdAt, updatedAt
                - `sortOrder` (optional): Sort direction ASC/DESC (default: ASC)
                
                **Returns:** Paginated list of parking lots matching the filter criteria
                """
    )
    public ResponseEntity<?> findAll(
            @Parameter(hidden = true)
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false, defaultValue = "id") String sortBy,
            @RequestParam(required = false, defaultValue = "ASC") String sortOrder,
            ParkingLotFilterParams params
    ) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(
                        ApiResponse.success(
                                "All parking lots found!",
                                parkingLotService.fetchAllParkingLots(
                                        userIdHeader,
                                        page, size, sortBy, sortOrder, params
                                ))
                );
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Get parking lot by ID",
            description = """
                Retrieve detailed information of a specific parking lot by its ID.
                
                **Path Parameters:**
                - `id` (required): The unique identifier of the parking lot
                
                **Returns:** Complete parking lot details including:
                - Basic information (name, address, location coordinates)
                - Operating hours and 24-hour status
                - Total floors and capacity
                - Current status
                - Associated lot capacities by vehicle type
                - Pricing rules configuration
                """
    )
    public ResponseEntity<?> findById(
            @Parameter(description = "ID of the parking lot to retrieve", required = true, example = "1")
            @PathVariable("id") Long id
    ) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(
                        ApiResponse.success(
                                "Fetch parking lot by id successfully",
                                parkingLotService.getParkingLotById(id)
                        )
                );
    }

    @PostMapping
    @Operation(

            summary = "Create a new parking lot",
            description = """
                Create a new parking lot for a specific partner with complete configuration.
                
                **Path Parameters:**
                - `partnerId` (required): The unique identifier of the partner who owns this parking lot
                
                **Request Body Fields:**
                - `name` (required): Name of the parking lot (max 255 characters)
                - `streetAddress` (required): Street address (max 255 characters)
                - `ward` (required): Ward or district (max 100 characters)
                - `city` (required): City name (max 100 characters)
                - `latitude` (required): Latitude coordinate (-90 to 90)
                - `longitude` (required): Longitude coordinate (-180 to 180)
                - `totalFloors` (required): Total number of floors (minimum 1)
                - `operatingHoursStart` (required): Opening time in HH:mm:ss format (e.g., "06:00:00")
                - `operatingHoursEnd` (required): Closing time in HH:mm:ss format (e.g., "23:00:00")
                - `is24Hour` (required): Boolean indicating 24-hour operation
                - `lotCapacityRequests` (optional): List of capacity configurations per floor and vehicle type
                - `pricingRuleCreateRequests` (optional): List of pricing rules for different vehicle types and time periods
                
                **Business Rules:**
                - Operating hours start and end times must be different
                - Initial status will be set to PENDING
                - Partner must exist in the system
                
                **Returns:** Created parking lot with assigned ID and initial status
                """
    )
    public ResponseEntity<?> addParkingLot(
            @Parameter(description = "ID of the partner creating this parking lot", required = true, example = "123")
            @RequestBody @Valid ParkingLotCreateRequest request,
            @Parameter(hidden = true)
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        ApiResponse.success(
                                "Parking Lot created successfully",
                                parkingLotService.addParkingLot(userIdHeader,request)
                        )
                );
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Update an existing parking lot",
            description = """
                Update parking lot information. All fields are optional - only include fields you want to update.
                
                **Path Parameters:**
                - `id` (required): The unique identifier of the parking lot to update
                
                **Request Body Fields (all optional):**
                - `name`: Name of the parking lot (max 255 characters)
                - `streetAddress`: Street address (max 255 characters)
                - `ward`: Ward or district (max 100 characters)
                - `city`: City name (max 100 characters)
                - `latitude`: Latitude coordinate (-90 to 90)
                - `longitude`: Longitude coordinate (-180 to 180)
                - `totalFloors`: Total number of floors (minimum 1)
                - `operatingHoursStart`: Opening time in HH:mm:ss format (e.g., "06:00:00")
                - `operatingHoursEnd`: Closing time in HH:mm:ss format (e.g., "23:00:00")
                - `is24Hour`: Boolean indicating 24-hour operation
                - `status`: Update parking lot status - see status descriptions below
                - `reason`: Required when status is REJECTED or MAP_DENIED - explanation for the status change
                
                **Status Update Options:**
                - **UNDER_SURVEY** - Set when parking lot needs evaluation
                - **PREPARING** - Set when infrastructure is being prepared
                - **REJECTED** - Set by admin when application is rejected (requires `reason`)
                - **PARTNER_CONFIGURATION** - Set when partner is configuring operations
                - **ACTIVE_PENDING** - Set when configuration complete, awaiting approval
                - **ACTIVE** - Set by admin to make parking lot operational
                - **INACTIVE** - Set when temporarily closing parking lot
                - **UNDER_MAINTENANCE** - Set when parking lot needs maintenance
                - **MAP_DENIED** - Set by admin when location is denied (requires `reason`)
                
                **Important Notes:**
                - Not all status transitions are allowed - follow the business workflow
                - When setting status to REJECTED or MAP_DENIED, the `reason` field is required
                - Only admins can set certain statuses (ACTIVE, REJECTED, MAP_DENIED)
                - Partners can only update operational details and limited status changes
                
                **Returns:** Updated parking lot information
                """
    )
    public ResponseEntity<?> updateParkingLot(
            @Parameter(description = "ID of the parking lot to update", required = true, example = "1")
            @PathVariable("id") Long id,
            @RequestBody @Valid ParkingLotUpdateRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(
                        ApiResponse.success(
                                "Parking Lot updated successfully",
                                parkingLotService.updateParkingLot(id, request)
                        )
                );
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Delete (deactivate) a parking lot",
            description = """
                Soft delete a parking lot by setting its status to INACTIVE.
                This operation does not permanently remove the parking lot from the database.
                
                **Path Parameters:**
                - `id` (required): The unique identifier of the parking lot to deactivate
                
                **Behavior:**
                - Changes parking lot status to INACTIVE
                - Preserves all historical data and configurations
                - Parking lot will no longer accept new bookings
                - Existing active bookings may need to be handled separately
                - Can be reactivated later by updating status back to ACTIVE
                
                **Use Cases:**
                - Temporarily closing a parking lot
                - Removing a parking lot from active listings
                - Seasonal closures
                
                **Returns:** Confirmation message with updated parking lot status
                """
    )
    public ResponseEntity<?> deleteParkingLot(
            @Parameter(description = "ID of the parking lot to deactivate", required = true, example = "1")
            @PathVariable("id") Long id
    ) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(
                        ApiResponse.success(
                                "Parking Lot Inactive",
                                parkingLotService.deleteParkingLot(id)
                        )
                );
    }

    @GetMapping("/nearby")
    @Operation(
            summary = "Get nearby parking lots based on location",
            description = """
                Find parking lots within a specified radius from a given geographic location.
                Uses the Haversine formula to calculate distances between coordinates.
                
                **Query Parameters:**
                - `latitude` (required): Latitude coordinate of the search center point (-90 to 90)
                - `longitude` (required): Longitude coordinate of the search center point (-180 to 180)
                - `radiusKm` (required): Search radius in kilometers (e.g., 5.0 for 5km radius)
                
                **How it works:**
                - Calculates the straight-line distance from the provided coordinates to each parking lot
                - Returns only parking lots within the specified radius
                - Results are typically sorted by distance (nearest first)
                - Only includes ACTIVE parking lots that are currently operational
                
                **Returns:** List of nearby parking lots including:
                - Parking lot details (name, address, location)
                - Distance from search point in kilometers
                - Available capacity and pricing information
                - Operating hours and 24-hour status
                - Current availability status
                
                **Use Cases:**
                - Mobile app "Find parking near me" feature
                - Navigation and route planning
                - Checking parking availability in a specific area
                - Location-based parking recommendations
                
                **Example:**
                - Search for parking within 2km of current location
                - Find parking near a specific address or landmark
                - Discover parking options in an unfamiliar area
                """
    )
    public ResponseEntity<?> getNearbyParkingLots(
            @Parameter(
                    description = "Latitude coordinate of search center point (decimal degrees)",
                    required = true,
                    example = "10.7827500",
                    schema = @Schema(type = "number", format = "double", minimum = "-90", maximum = "90")
            )
            @RequestParam("latitude") Double latitude,

            @Parameter(
                    description = "Longitude coordinate of search center point (decimal degrees)",
                    required = true,
                    example = "106.6986700",
                    schema = @Schema(type = "number", format = "double", minimum = "-180", maximum = "180")
            )
            @RequestParam("longitude") Double longitude,

            @Parameter(
                    description = "Search radius in kilometers. Recommended values: 1-10 km for urban areas",
                    required = true,
                    example = "5.0",
                    schema = @Schema(type = "number", format = "double", minimum = "0.1", maximum = "50")
            )
            @RequestParam("radiusKm") Double radiusKm
    ) {
        return ResponseEntity.status(HttpStatus.OK).body(
                ApiResponse.success(
                        "Fetch nearby Parking Lots successfully",
                        parkingLotService.fetchNearbyParkingLots(latitude, longitude, radiusKm)
                )
        );
    }

    @GetMapping("/count")
    public ResponseEntity<?> countParkingLots() {
        return ResponseEntity.status(HttpStatus.OK).body(
                ApiResponse.success("Count parking lots successfully", parkingLotService.count())
        );
    }

    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "Import parking lots from Excel file",
            description = """
                    Import multiple parking lot records from an Excel file (.xlsx format).
                    
                    **File Requirements:**
                    - Format: Excel (.xlsx)
                    - Maximum file size: 50 MB
                    - Maximum records: 10,000 per file
                    
                    **Excel Column Order:**
                    1. Partner ID (Long)
                    2. Name (String)
                    3. Street Address (String)
                    4. Ward (String)
                    5. City (String)
                    6. Latitude (Double, -90 to 90)
                    7. Longitude (Double, -180 to 180)
                    8. Total Floors (Integer)
                    9. Operating Hours Start (Time, format: HH:mm:ss)
                    10. Operating Hours End (Time, format: HH:mm:ss)
                    11. Is 24 Hour (Boolean: true/false)
                    12. Boundary Top Left X (Double)
                    13. Boundary Top Left Y (Double)
                    14. Boundary Width (Double)
                    15. Boundary Height (Double)
                    16. Status (String: PENDING, ACTIVE, etc.)
                    17. Reason (String, optional)
                    
                    **Process:**
                    - First row should be headers (will be skipped)
                    - Records are imported in batches of 500 for optimal performance
                    - Invalid rows are logged but don't stop the import
                    - Transaction is committed after successful import
                    
                    **Returns:** Import summary including:
                    - Number of successfully imported records
                    - Number of failed records
                    - List of errors with row numbers
                    
                    **Example Excel Data:**
                    | Partner ID | Name | Street Address | Ward | City | Latitude | Longitude | ... |
                    |------------|------|----------------|------|------|----------|-----------|-----|
                    | 1 | Plaza Parking | 123 Main St | Ward 1 | HCMC | 10.7827 | 106.6987 | ... |
                    """,
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Excel file containing parking lot data",
                    required = true,
                    content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE)
            )
    )
    public ResponseEntity<?> importParkingLots(
            @Parameter(description = "Excel file (.xlsx) with parking lot data")
            @RequestParam("file") MultipartFile file
    ) {
        try {
            // Validate file
            if (file.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error(ErrorCode.UNCATEGORIZED_EXCEPTION.name(), "File is empty"));
            }

            String filename = file.getOriginalFilename();
            if (filename == null || !filename.endsWith(".xlsx")) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error(ErrorCode.UNCATEGORIZED_EXCEPTION.name(), "Filename is invalid"));
            }

            // Import data
            ParkingLotImportService.ImportResult result = importService.importFromExcel(file);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(
                            "Successfully imported " + filename,
                            result
                    ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(ErrorCode.UNCATEGORIZED_EXCEPTION.name(), e.getMessage()));
        }
    }

    @GetMapping("/export")
    @Operation(
            summary = "Export all parking lots to Excel",
            description = """
                    Export all parking lots to an Excel file (.xlsx format).
                    
                    **Returns:** Excel file containing:
                    - All parking lot records
                    - Complete data including ID, partner info, location, operating hours
                    - Status and timestamps
                    - Formatted and styled spreadsheet
                    
                    **File Format:**
                    - Extension: .xlsx
                    - Encoding: UTF-8
                    - Includes header row with column names
                    - Auto-sized columns for readability
                    
                    **Use Cases:**
                    - Data backup and archival
                    - Offline analysis and reporting
                    - Data migration to other systems
                    - Sharing data with partners or stakeholders
                    """
    )
    public ResponseEntity<?> exportAllParkingLots() {
        try {
            byte[] excelData = exportService.exportAllToExcel();

            String filename = "parking_lots_" +
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) +
                    ".xlsx";

            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename);
            headers.add(HttpHeaders.CONTENT_TYPE, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

            return ResponseEntity.ok()
                    .headers(headers)
                    .contentLength(excelData.length)
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(new ByteArrayResource(excelData));

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(ErrorCode.UNCATEGORIZED_EXCEPTION.name(), e.getMessage()));
        }
    }
}