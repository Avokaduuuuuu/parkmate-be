package com.parkmate.user.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class UserSearchCriteria {

    private Long id;
    private String phone;
    private String firstName;
    private String lastName;
    private String fullName;
    private String address;
    private String idNumber;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateOfBirthFrom;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateOfBirthTo;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAtFrom;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAtTo;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAtFrom;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAtTo;

    private Long accountId;

    // Pagination
    private Integer page = 0;
    private Integer size = 10;
    private String sortBy = "id";
    private String sortDirection = "ASC";
}
