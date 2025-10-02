package com.parkmate.partner.dto;

import com.parkmate.common.util.ParseUtil;
import com.parkmate.partner.PartnerStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PartnerSearchRequest {

    // Smart fields (multi-value input dạng string "1,2,3")
    private String partnerIds;
    private String statusList;

    // Single fields
    private String companyName;
    private String taxNumber;
    private String companyEmail;
    private String companyPhone;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAfter;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdBefore;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAfter;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedBefore;

    public PartnerSearchCriteria toCriteria() {
        PartnerSearchCriteria.PartnerSearchCriteriaBuilder builder = PartnerSearchCriteria.builder();

        // Parse partnerIds
        if (StringUtils.hasText(this.partnerIds)) {
            List<Long> idList = ParseUtil.parseLongList(this.partnerIds, "partnerIds");
            if (idList.size() == 1) {
                builder.partnerId(idList.get(0));
            } else {
                builder.partnerIds(idList);
            }
        }

        // Parse statusList
        if (StringUtils.hasText(this.statusList)) {
            List<PartnerStatus> statuses = ParseUtil.parseEnumList(this.statusList, PartnerStatus.class, "statusList");
            if (statuses.size() == 1) {
                builder.status(statuses.get(0));
            } else {
                builder.statusList(statuses);
            }
        }

        builder.companyName(this.companyName)
                .taxNumber(this.taxNumber)
                .companyEmail(this.companyEmail)
                .companyPhone(this.companyPhone)
                .createdAfter(this.createdAfter)
                .createdBefore(this.createdBefore)
                .updatedAfter(this.updatedAfter)
                .updatedBefore(this.updatedBefore);

        return builder.build();
    }
}
