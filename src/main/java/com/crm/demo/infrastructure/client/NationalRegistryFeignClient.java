package com.crm.demo.infrastructure.client;

import com.crm.demo.domain.LeadDto;
import feign.Headers;
import feign.RequestLine;
import org.springframework.web.bind.annotation.PathVariable;


@Headers( { "Accept: application/json",
            "Content-Type: application/json" } )
public interface NationalRegistryFeignClient
{
    @RequestLine( "GET /api/v1/national-registry/{leadId}" )
    LeadDto getLeadFromNationalRegistry( @PathVariable int leadId );
}
