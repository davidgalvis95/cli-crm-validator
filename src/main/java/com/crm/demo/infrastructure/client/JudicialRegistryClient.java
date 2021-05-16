package com.crm.demo.infrastructure.client;

import com.crm.demo.domain.JudicialRecordsDto;
import feign.Headers;
import feign.RequestLine;
import org.springframework.web.bind.annotation.PathVariable;

@Headers( { "Accept: application/json",
            "Content-Type: application/json" } )
public interface JudicialRegistryClient
{
    @RequestLine( "GET /api/v1/judicial-registry/{leadId}")
    JudicialRecordsDto getJudicialRecordsByLeadId( @PathVariable int leadId);
}
