package com.parkmate.rating;

import com.parkmate.rating.dto.req.RatingCreateRequest;
import com.parkmate.rating.dto.req.RatingUpdateRequest;
import com.parkmate.rating.dto.resp.RatingResponse;
import org.springframework.data.domain.Page;

public interface RatingService {
    Page<RatingResponse> getRatings(
            int page,
            int size,
            String sortBy,
            String sortOrder,
            RatingFilterParams filterParams,
            String userHeaderId
    );

    RatingResponse getRatingById(Long id);

    RatingResponse createRating(Long lotId, RatingCreateRequest request, String userIdHeader);
    RatingResponse updateRating(Long id, RatingUpdateRequest request);
    void deleteRating(Long id);
    Long countRatings();
}
