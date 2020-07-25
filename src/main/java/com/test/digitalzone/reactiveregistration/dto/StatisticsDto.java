package com.test.digitalzone.reactiveregistration.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StatisticsDto {
    private Long count;
    private Long unique;
    private Long regularUsers;
}
