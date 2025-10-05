package com.parkmate.partner;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.parkmate.account.Account;
import com.parkmate.partnerRegistration.PartnerRegistration;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "partner")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Partner entity representing parking lot operators")
public class Partner {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Partner unique identifier", example = "1")
    private Long id;

    @Column(name = "company_name", nullable = false)
    @Schema(description = "Company name", example = "ABC Parking Solutions Ltd.")
    private String companyName;

    @Column(name = "tax_number", unique = true, nullable = false, length = 50)
    @Schema(description = "Tax identification number", example = "0123456789")
    private String taxNumber;

    @Column(name = "business_license_number", nullable = false, length = 100)
    @Schema(description = "Business license number", example = "BL-2024-001234")
    private String businessLicenseNumber;

    @Column(name = "business_license_file_url", length = 500)
    @Schema(description = "URL to business license file", example = "https://storage.parkmate.com/licenses/abc-license.pdf")
    private String businessLicenseFileUrl;

    @Column(name = "company_address", nullable = false, columnDefinition = "TEXT")
    @Schema(description = "Complete company address", example = "123 Nguyen Hue Street, District 1, Ho Chi Minh City")
    private String companyAddress;

    @Column(name = "company_phone", length = 20)
    @Schema(description = "Company contact phone", example = "+84-28-1234-5678")
    private String companyPhone;

    @Column(name = "company_email")
    @Schema(description = "Company contact email", example = "contact@abcparking.com")
    private String companyEmail;

    @Column(name = "business_description", columnDefinition = "TEXT")
    @Schema(description = "Description of business operations", example = "Providing smart parking solutions for commercial buildings")
    private String businessDescription;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "status", nullable = false)
    @Schema(description = "Partner status", example = "APPROVED", allowableValues = {"APPROVED", "SUSPENDED", "DELETED"})
    private PartnerStatus status;

    @Column(name = "suspension_reason", columnDefinition = "TEXT")
    @Schema(description = "Reason for suspension if status is SUSPENDED")
    private String suspensionReason;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "Partner creation timestamp", example = "2024-09-23 14:30:00")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "Last update timestamp", example = "2024-09-23 16:45:00")
    private LocalDateTime updatedAt;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approval_request_id", referencedColumnName = "id")
    private PartnerRegistration partnerRegistration;

    @OneToMany(mappedBy = "partner", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Schema(description = "List of accounts associated with this partner")
    private List<Account> accounts;

    @Schema(hidden = true)
    public boolean isActive() {
        return PartnerStatus.APPROVED.equals(this.status);
    }

    @Schema(hidden = true)
    public boolean isSuspended() {
        return PartnerStatus.SUSPENDED.equals(this.status);
    }
}
