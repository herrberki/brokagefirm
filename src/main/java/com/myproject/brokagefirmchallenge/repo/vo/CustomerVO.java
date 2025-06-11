package com.myproject.brokagefirmchallenge.repo.vo;

import com.myproject.brokagefirmchallenge.repo.enumtype.CustomerStatus;
import com.myproject.brokagefirmchallenge.repo.enumtype.UserRole;
import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Builder
public class CustomerVO extends BaseResponse {

    private Long customerId;
    private String username;
    private String email;
    private String fullName;
    private String phoneNumber;
    private UserRole role;
    private CustomerStatus status;
    private LocalDateTime lastLoginDate;
    private LocalDateTime createdDate;
}
