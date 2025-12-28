package com.parkmate.rating;

import com.parkmate.common.ApiResponse;
import com.parkmate.rating.dto.req.RatingCreateRequest;
import com.parkmate.rating.dto.req.RatingUpdateRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/v1/parking-service/ratings")
@RequiredArgsConstructor
@Tag(name = "Rating API", description = "API for managing parking lot ratings and reviews")
public class RatingController {

    private final RatingService ratingService;

    @GetMapping
    @Operation(
            summary = "Get all ratings with filtering and pagination",
            description = """
                    Search and retrieve parking lot ratings with comprehensive filtering and pagination support.
                    
                    **Query Parameters (Filters):**
                    - `lotId` (optional): Filter by parking lot ID
                      * Returns only ratings for the specified parking lot
                      * Example: lotId=1
                      * Use case: View all ratings for a specific parking lot
                    
                    - `userId` (optional): Filter by user ID
                      * Returns only ratings created by the specified user
                      * Example: userId=1001
                      * Use case: View all ratings by a specific user
                    
                    - `overallRating` (optional): Filter by exact rating value
                      * Returns ratings matching the exact star rating
                      * Values: 1, 2, 3, 4, or 5
                      * Example: overallRating=5
                      * Use case: Find all 5-star or 1-star reviews
                    
                    - `ratingLessThan` (optional): Filter ratings less than or equal to value
                      * Returns ratings with stars ≤ specified value
                      * Values: 1, 2, 3, 4, or 5
                      * Example: ratingLessThan=2
                      * Use case: Find negative reviews (1-2 stars)
                    
                    - `ratingGreaterThan` (optional): Filter ratings greater than or equal to value
                      * Returns ratings with stars ≥ specified value
                      * Values: 1, 2, 3, 4, or 5
                      * Example: ratingGreaterThan=4
                      * Use case: Find positive reviews (4-5 stars)
                    
                    - `isVisible` (optional): Filter by visibility status
                      * true - Show only visible/published ratings
                      * false - Show only hidden/moderated ratings
                      * Example: isVisible=true
                      * Use case: Admin moderation, public display filtering
                    
                    - `createdBefore` (optional): Filter ratings created before date/time
                      * Format: ISO 8601 (yyyy-MM-dd'T'HH:mm:ss)
                      * Example: createdBefore=2025-11-01T00:00:00
                      * Use case: Historical data analysis, reporting
                    
                    - `createdAfter` (optional): Filter ratings created after date/time
                      * Format: ISO 8601 (yyyy-MM-dd'T'HH:mm:ss)
                      * Example: createdAfter=2025-10-01T00:00:00
                      * Use case: Recent reviews, trend analysis
                    
                    **Pagination & Sorting:**
                    - `page` (optional): Page number, 0-indexed (default: 0)
                    - `size` (optional): Records per page (default: 10)
                    - `sortBy` (optional): Sort field (default: createdAt)
                      * Available: id, overallRating, createdAt, updatedAt
                    - `sortOrder` (optional): Sort direction ASC/DESC (default: DESC)
                      * DESC shows newest/highest first
                    
                    **Example Queries:**
                    - Get all 5-star reviews for lot 1: 
                      `?lotId=1&overallRating=5&isVisible=true`
                    
                    - Get negative reviews (≤2 stars):
                      `?ratingLessThan=2&isVisible=true&sortBy=createdAt&sortOrder=DESC`
                    
                    - Get recent reviews (last 30 days):
                      `?createdAfter=2025-10-18T00:00:00&sortBy=createdAt&sortOrder=DESC`
                    
                    - Get user's rating history:
                      `?userId=1001&sortBy=createdAt&sortOrder=DESC`
                    
                    - Get hidden/moderated reviews:
                      `?isVisible=false&sortBy=createdAt&sortOrder=DESC`
                    
                    **Returns:** Paginated list of ratings with complete information including:
                    - Rating ID and user information
                    - Associated parking lot details
                    - Star rating and review content
                    - Visibility status
                    - Creation and update timestamps
                    """
    )
    public ResponseEntity<?> getAllRatings(
            @Parameter(description = "Page number (0-indexed)", example = "0")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Number of records per page", example = "10")
            @RequestParam(defaultValue = "10") int size,

            @Parameter(description = "Field to sort by", example = "createdAt")
            @RequestParam(required = false, defaultValue = "createdAt") String sortBy,

            @Parameter(description = "Sort direction (ASC or DESC)", example = "DESC")
            @RequestParam(required = false, defaultValue = "DESC") String sortOrder,
            @Parameter(hidden = true)
            @RequestHeader(value = "X-User-Id", required = false)
            Optional<String> userIdHeader,
            RatingFilterParams filterParams
    ) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(
                        ApiResponse.success(
                                "All ratings retrieved successfully",
                                ratingService.getRatings(page, size, sortBy, sortOrder, filterParams,
                                        userIdHeader.filter(s -> !s.isEmpty()).orElse(null))
                        )
                );
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Get rating by ID",
            description = """
                    Retrieve detailed information of a specific rating by its ID.
                    
                    **Path Parameters:**
                    - `id` (required): The unique identifier of the rating
                    
                    **Returns:** Complete rating details including:
                    - Rating ID and timestamp information
                    - User ID who created the rating
                    - Associated parking lot information
                    - Overall rating (1-5 stars)
                    - Review title and detailed comment
                    - Visibility status (visible/hidden)
                    - Creation and last update timestamps
                    
                    **Use Cases:**
                    - Displaying full review details
                    - Moderation review
                    - User viewing their own ratings
                    - Generating rating reports
                    
                    **Error Responses:**
                    - 404: Rating not found with the specified ID
                    """
    )
    public ResponseEntity<?> getRatingById(
            @Parameter(description = "ID of the rating to retrieve", required = true, example = "1")
            @PathVariable("id") Long id
    ) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(
                        ApiResponse.success(
                                "Rating retrieved successfully",
                                ratingService.getRatingById(id)
                        )
                );
    }

    @PostMapping("/lot/{lotId}")
    @Operation(
            summary = "Create a new rating for a parking lot",
            description = """
                    Create a new rating/review for a specific parking lot.
                    
                    **Path Parameters:**
                    - `lotId` (required): The ID of the parking lot being rated
                    
                    **Request Body Fields:**
                    - `userId` (required): ID of the user creating this rating
                      * Must be a valid user ID from the user service
                      * Each user can only have ONE rating per parking lot
                      * Attempting to create duplicate will return error
                    
                    - `overallRating` (required): Star rating for the parking lot
                      * Valid values: 1, 2, 3, 4, or 5
                      * 1 star = Very poor experience
                      * 2 stars = Poor experience
                      * 3 stars = Average experience
                      * 4 stars = Good experience
                      * 5 stars = Excellent experience
                      * Required field, cannot be null
                    
                    - `title` (optional): Short summary of the review
                      * Max 200 characters
                      * Examples: "Excellent parking facility", "Clean and secure"
                      * Displayed as heading in review listings
                    
                    - `comment` (optional): Detailed review text
                      * Free-form text field
                      * Can include specific feedback about:
                        - Cleanliness
                        - Security measures
                        - Staff friendliness
                        - Pricing fairness
                        - Accessibility
                        - Facility condition
                      * Recommended for ratings below 4 stars
                    
                    **Business Rules:**
                    - Each user can only create ONE rating per parking lot
                    - If user already has a rating for this lot, update it instead
                    - Initial visibility status is set to TRUE (visible)
                    - Parking lot must exist and be active
                    - Rating can be created even without a comment
                    
                    **Validation:**
                    - Rating must be between 1 and 5 (inclusive)
                    - User ID must be valid
                    - Parking lot must exist
                    
                    **Returns:** Created rating with assigned ID and timestamps
                    
                    **Example Request Body:**
```json
                    {
                      "userId": 1001,
                      "overallRating": 5,
                      "title": "Excellent parking facility",
                      "comment": "Very clean and well-maintained. Security is great! Easy entry and exit with BLE. Highly recommend for daily parking."
                    }
```
                    
                    **Error Responses:**
                    - 400: Invalid rating value or missing required fields
                    - 404: Parking lot not found
                    - 409: User already has a rating for this parking lot (use update instead)
                    """
    )
    public ResponseEntity<?> createRating(
            @Parameter(description = "ID of the parking lot being rated", required = true, example = "1")
            @PathVariable("lotId") Long lotId,
            @Parameter(hidden = true)
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
            @RequestBody @Valid RatingCreateRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        ApiResponse.success(
                                "Rating created successfully",
                                ratingService.createRating(lotId, request, userIdHeader)
                        )
                );
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Update an existing rating",
            description = """
                    Update rating information. All fields are optional - only include fields you want to update.
                    
                    **Path Parameters:**
                    - `id` (required): The unique identifier of the rating to update
                    
                    **Request Body Fields (all optional):**
                    - `overallRating`: Update the star rating
                      * Valid values: 1, 2, 3, 4, or 5
                      * Can be changed if user's experience changed
                      * Example: User initially gave 3 stars, now upgrading to 5 stars
                    
                    - `title`: Update the review title
                      * Max 200 characters
                      * Can modify to better reflect updated opinion
                    
                    - `comment`: Update the detailed review text
                      * Can add more details to existing review
                      * Can modify based on new experiences
                      * Can clarify previous feedback
                    
                    - `isVisible`: Update visibility status
                      * true - Make rating visible to public
                      * false - Hide rating (moderation, inappropriate content)
                      * Typically used by admins for content moderation
                      * Users cannot hide their own ratings (only admins)
                    
                    **Use Cases:**
                    
                    1. **User Updates Their Review:**
                       - User returns to parking lot and has better experience
                       - User wants to add more details to their review
                       - User made a typo and wants to correct it
                    
                    2. **Admin Moderation:**
                       - Hide inappropriate or spam reviews: `{"isVisible": false}`
                       - Restore hidden reviews after review: `{"isVisible": true}`
                    
                    3. **Partial Updates:**
                       - Update just rating: `{"overallRating": 5}`
                       - Update just comment: `{"comment": "Additional feedback..."}`
                       - Update multiple fields at once
                    
                    **Important Notes:**
                    - Only include fields you want to change
                    - Fields not included will keep their current values
                    - Cannot change userId or parkingLotId (create new rating instead)
                    - Updated timestamp will be automatically set
                    
                    **Returns:** Updated rating with new values and updated timestamp
                    
                    **Example Request Bodies:**
                    
                    User updating their review:
```json
                    {
                      "overallRating": 5,
                      "title": "Much improved!",
                      "comment": "Original review was 3 stars. Came back this week and noticed major improvements. Cleaner facilities, better lighting, more security cameras. Upgrading to 5 stars!"
                    }
```
                    
                    Admin hiding inappropriate content:
```json
                    {
                      "isVisible": false
                    }
```
                    
                    **Error Responses:**
                    - 400: Invalid rating value (not 1-5)
                    - 404: Rating not found with specified ID
                    """
    )
    public ResponseEntity<?> updateRating(
            @Parameter(description = "ID of the rating to update", required = true, example = "1")
            @PathVariable("id") Long id,

            @RequestBody @Valid RatingUpdateRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(
                        ApiResponse.success(
                                "Rating updated successfully",
                                ratingService.updateRating(id, request)
                        )
                );
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Delete a rating",
            description = """
                    Permanently delete a rating from the system.
                    
                    **Path Parameters:**
                    - `id` (required): The unique identifier of the rating to delete
                    
                    **Behavior:**
                    - Permanently removes rating record from database
                    - Cannot be undone - this is a hard delete
                    - All rating data and history will be lost
                    - Will affect parking lot's average rating calculation
                    
                    **Important Considerations:**
                    - ⚠️ **Warning:** This action cannot be reversed
                    - Consider setting `isVisible=false` instead for moderation
                    - Deleting removes rating permanently from statistics
                    - May impact parking lot's overall rating score
                    
                    **Who Can Delete:**
                    - **Users:** Can delete their own ratings
                      * Use case: User wants to completely remove their review
                      * Alternative: Update the rating instead
                    
                    - **Admins:** Can delete any rating
                      * Use case: Spam, fake reviews, policy violations
                      * Alternative: Set isVisible=false to hide instead
                    
                    **Recommended Alternatives:**
                    - For inappropriate content: Set `isVisible=false`
                    - For outdated reviews: User can update with current experience
                    - For disputes: Hide temporarily while investigating
                    
                    **Impact on Statistics:**
                    - Parking lot's average rating will be recalculated
                    - Total rating count will decrease
                    - May affect parking lot's ranking in search results
                    
                    **Use Cases:**
                    - User removes rating by mistake and wants fresh start
                    - Admin removes spam or fake reviews
                    - Removing test data from development
                    - User account deletion (cleanup user's ratings)
                    
                    **Returns:** Confirmation message of successful deletion
                    
                    **Error Responses:**
                    - 404: Rating not found with specified ID
                    - 403: User not authorized to delete this rating
                    """
    )
    public ResponseEntity<?> deleteRating(
            @Parameter(description = "ID of the rating to delete", required = true, example = "1")
            @PathVariable("id") Long id
    ) {
        ratingService.deleteRating(id);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(
                        ApiResponse.success(
                                "Rating deleted successfully",
                                null
                        )
                );
    }


    @GetMapping("/count")
    @Operation(
            summary = "Count total ratings"
    )
    public ResponseEntity<?> countRatings() {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(
                        ApiResponse.success(
                                "Rating count retrieved successfully",
                                ratingService.countRatings()
                        )
                );
    }
}