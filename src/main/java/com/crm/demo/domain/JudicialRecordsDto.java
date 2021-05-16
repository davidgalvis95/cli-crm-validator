package com.crm.demo.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import lombok.ToString;


@Data
@Builder
@ToString
@AllArgsConstructor
public class JudicialRecordsDto
      extends ExternalResponses
{
    Integer id;

    Boolean hasJudicialRecords;
}
