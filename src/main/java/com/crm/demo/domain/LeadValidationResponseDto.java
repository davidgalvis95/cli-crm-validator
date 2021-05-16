package com.crm.demo.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.ToString;
import lombok.Value;


@Value
@Builder
@ToString
@AllArgsConstructor
public class LeadValidationResponseDto
{
    Lead lead;

    Integer score;

    Boolean isAProspect;

    String reasonMessage;
}
