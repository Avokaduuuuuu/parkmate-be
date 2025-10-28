package com.parkmate.area;

import com.parkmate.area.dto.req.AreaCreateRequest;
import com.parkmate.area.dto.req.AreaUpdateRequest;
import com.parkmate.area.dto.resp.AreaDetailedResponse;
import com.parkmate.area.dto.resp.AreaResponse;
import org.springframework.data.domain.Page;

public interface AreaService {
    Page<AreaResponse> findAllAreas(
            int page, int size, String sortBy, String sortOrder, AreaFilterParams params
    );

    AreaDetailedResponse findAreaById(Long id);

    AreaResponse createArea(AreaCreateRequest request, Long floorId);
    AreaResponse updateArea(AreaUpdateRequest request, Long id);
    void deleteArea(Long id);

    Long count();
}
