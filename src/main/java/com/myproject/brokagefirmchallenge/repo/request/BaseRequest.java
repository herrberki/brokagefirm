package com.myproject.brokagefirmchallenge.repo.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.io.Serializable;

@Data
public abstract class BaseRequest implements Serializable {

    @JsonIgnore
    private Long customerId;

    @JsonIgnore
    private String requestId;
}

