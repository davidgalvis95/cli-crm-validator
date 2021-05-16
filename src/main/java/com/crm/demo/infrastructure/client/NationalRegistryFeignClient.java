package com.crm.demo.infrastructure.client;

import com.crm.demo.domain.Lead;
import com.crm.demo.domain.LeadDto;
import feign.Headers;
import feign.RequestLine;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;


@Headers( { "Accept: application/json",
            "Content-Type: application/json" } )
public interface NationalRegistryFeignClient
{
    @RequestLine( "GET /api/v1/national-registry/findLead" )
    @ResponseBody
    ResponseEntity<LeadDto> getLeadFromNationalRegistry( @RequestBody Lead leadId );
}
