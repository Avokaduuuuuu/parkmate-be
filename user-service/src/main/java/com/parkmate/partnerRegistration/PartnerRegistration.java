package com.parkmate.partnerRegistration;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.parkmate.account.Account;
import com.parkmate.common.enums.RequestStatus;
import com.parkmate.partner.Partner;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

@Entity
@Table(name = "partner_registration")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Partner registration request entity")
public class PartnerRegistration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Request unique identifier", example = "1")
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

    @Column(name = "contact_person_name", nullable = false)
    @Schema(description = "Contact person full name", example = "Nguyen Van A")
    private String contactPersonName;

    @Column(name = "contact_person_phone", nullable = false)
    @Schema(description = "Contact person phone number", example = "+84-901-234-567")
    private String contactPersonPhone;

    @Column(name = "contact_person_email", nullable = false)
    @Schema(description = "Contact person email", example = "nguyen.vana@abcparking.com")
    private String contactPersonEmail;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "status", nullable = false)
    @Builder.Default
    @Schema(description = "Request status", example = "PENDING", allowableValues = {"PENDING", "APPROVED", "REJECTED"})
    private RequestStatus status = RequestStatus.PENDING;

    @Column(name = "submitted_at")
    @Builder.Default
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "Request submission timestamp", example = "2024-09-23 10:30:00")
    private LocalDateTime submittedAt = LocalDateTime.now();

    @Schema(description = "ID of admin who reviewed this request", example = "456")
    @Column(name = "reviewed_by")
    private Long reviewedBy;

    @Column(name = "reviewed_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "Request review timestamp", example = "2024-09-23 14:30:00")
    private LocalDateTime reviewedAt;

    @Column(name = "approval_notes", columnDefinition = "TEXT")
    @Schema(description = "Notes from admin during approval", example = "All documents verified successfully")
    private String approvalNotes;

    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    @Schema(description = "Reason for rejection if status is REJECTED", example = "Business license is expired")
    private String rejectionReason;

    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by", insertable = false, updatable = false)
    @Schema(description = "Admin account that reviewed this request")
    private Account reviewer;

    @OneToOne(mappedBy = "partnerRegistration", cascade = CascadeType.ALL)
    @Schema(description = "Partner created from this request (if approved)")
    private Partner partner;

    // Helper methods
    @Schema(hidden = true)
    public boolean isPending() {
        return RequestStatus.PENDING.equals(this.status);
    }

    @Schema(hidden = true)
    public boolean isApproved() {
        return RequestStatus.APPROVED.equals(this.status);
    }

    @Schema(hidden = true)
    public boolean isRejected() {
        return RequestStatus.REJECTED.equals(this.status);
    }

}
