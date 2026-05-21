package com.muyulu.mboard.service.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RateRequest {

    @Min(value = 1, message = "Star must be between 1 and 5")
    @Max(value = 5, message = "Star must be between 1 and 5")
    private Integer star;
}
