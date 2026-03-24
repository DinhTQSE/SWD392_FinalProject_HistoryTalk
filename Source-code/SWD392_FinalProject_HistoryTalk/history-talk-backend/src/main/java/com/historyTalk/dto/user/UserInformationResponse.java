package com.historyTalk.dto.user;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class UserInformationResponse {
    private UUID userId;
    private String full_name;
    private String dob;
    private String gender;
    private String phoneNumber;
    private String address;
    private String email;
    private String updatedBy;
    private String avatarUrl;
    private LocalDateTime deletedAt;
}
