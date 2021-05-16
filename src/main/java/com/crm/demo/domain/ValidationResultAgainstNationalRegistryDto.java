package com.crm.demo.domain;

import lombok.Builder;
import lombok.Value;


@Value
@Builder
public class ValidationResultAgainstNationalRegistryDto
{
    int id;

    Boolean isValid;

    String reason;
}
