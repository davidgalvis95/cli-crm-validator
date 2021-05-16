package com.crm.demo.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Value;


@Data
@Builder
@AllArgsConstructor
public class JudicialRecordsDto extends ExternalResponses
{
    Integer id;

    Boolean hasJudicialRecords;
}
