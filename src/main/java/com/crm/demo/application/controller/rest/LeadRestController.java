package com.crm.demo.application.controller.rest;

import com.crm.demo.domain.Lead;
import com.crm.demo.domain.LeadDto;
import com.crm.demo.domain.LeadValidationResponseDto;
import com.crm.demo.domain.service.ValidationService;
import com.crm.demo.infrastructure.client.NationalRegistryFeignClient;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;


@RestController
@RequestMapping( value = "/api/v1",
                 consumes = { APPLICATION_JSON_VALUE },
                 produces = { APPLICATION_JSON_VALUE } )
@AllArgsConstructor
public class LeadRestController
{
    private final ValidationService leadValidationService;

    com.crm.demo.infrastructure.client.NationalRegistryFeignClient nationalRegistryFeignClient;

    private static final Map<Integer, String> REASONS_RESULT_MAP = Collections.unmodifiableMap( reasonsMap() );


    @GetMapping( "/validate/{leadId}" )
    public ResponseEntity<LeadValidationResponseDto> validateLead( @PathVariable final Integer leadId,
                                                                   @RequestParam final boolean isSampleLead )
    {
        final Pattern digitPattern = Pattern.compile( "\\d{9}" );
        if ( digitPattern.matcher( leadId.toString() ).matches() )
        {
            nationalRegistryFeignClient.getLeadFromNationalRegistry( Lead.builder().idNumber( leadId ).build() ); 
            LeadValidationResponseDto leadValidationResponseDto = leadValidationService.validateLead( leadId, isSampleLead );
            if ( REASONS_RESULT_MAP.get( 4 ).equals( leadValidationResponseDto.getReasonMessage() ) ||
                 REASONS_RESULT_MAP.get( 5 ).equals( leadValidationResponseDto.getReasonMessage() ) )
            {
                return ResponseEntity.status( HttpStatus.INTERNAL_SERVER_ERROR ).body( leadValidationResponseDto );
            }
            return ResponseEntity.ok( leadValidationResponseDto );
        }
        return ResponseEntity.status( HttpStatus.BAD_REQUEST ).body( null );
    }


    private static Map<Integer, String> reasonsMap()
    {
        Map<Integer, String> map = new LinkedHashMap<>();
        map.put( 1, "The lead complies with the requested criteria" );
        map.put( 2, "The score of the lead is below the accepted limit" );
        map.put( 3, "Either data of the lead does not match national registry, or is reported in judicial registries" );
        map.put( 4, "Failed to get information from external systems" );
        map.put( 5, "Failed to process the validation" );
        return map;
    }
}
