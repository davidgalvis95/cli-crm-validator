package com.crm.demo.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;

import java.time.LocalDate;


@Data
@Builder
@ToString
@AllArgsConstructor
public class LeadDto
      extends ExternalResponses
{
    private Integer idNumber;

    private LocalDate birthDate;

    private String firstName;

    private String lastName;

    private String email;
}
