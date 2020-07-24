package com.test.digitalzone.reactiveregistration.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ViewEvent {
    private Long id;
    private String userId;
    private LocalDateTime time;
}
