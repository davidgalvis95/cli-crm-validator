package com.crm.demo.domain.service;

import com.crm.demo.domain.Lead;
import com.crm.demo.domain.LeadDto;
import com.crm.demo.domain.ValidationResultAgainstNationalRegistryDto;
import com.crm.demo.infrastructure.client.NationalRegistryClientMock;
import com.crm.demo.infrastructure.client.NationalRegistryFeignClient;
import com.crm.demo.infrastructure.exception.ExternalPublicServiceProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.HttpClient;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Objects;


@Slf4j
@Service
@AllArgsConstructor
public class NationalRegistryService
{
    private final NationalRegistryFeignClient nationalRegistryFeignClient;

    private final NationalRegistryClientMock nationalRegistryClientMock;


    public ValidationResultAgainstNationalRegistryDto validateLeadAgainstNationalRegistry( final Lead lead )
    {
        ResponseEntity<LeadDto> leadFromNationalRegistry;
        try
        {
            leadFromNationalRegistry = nationalRegistryFeignClient.getLeadFromNationalRegistry( lead );

            if ( leadFromNationalRegistry.getStatusCode() == HttpStatus.NOT_FOUND || leadFromNationalRegistry.getStatusCode() == HttpStatus.INTERNAL_SERVER_ERROR )
            {
                return validateResponse( lead, null );
            }
        }
        catch ( Exception e )
        {
            log.error( Arrays.toString( e.getStackTrace() ) + "====>" + e.getMessage() );
            throw new ExternalPublicServiceProcessingException( "ERROR: Failed to get record from national registry for leadId" + lead.getIdNumber() );
        }

        return validateResponse( lead, leadFromNationalRegistry.getBody() );
    }


    public ValidationResultAgainstNationalRegistryDto validateSampleLeadAgainstNationalRegistry( final Lead lead,
                                                                                                 final ObjectMapper objectMapper,
                                                                                                 final HttpClient client )
    {
        LeadDto leadFromNationalRegistry;
        try
        {
            leadFromNationalRegistry = nationalRegistryClientMock.getMockedResponseFromNationalService( lead.getIdNumber(), objectMapper, client );
        }
        catch ( Exception e )
        {
            log.error( Arrays.toString( e.getStackTrace() ) + "====>" + e.getMessage() );
            throw new ExternalPublicServiceProcessingException( "ERROR: Failed to get record from national sample registry for leadId" + lead.getIdNumber() );
        }

        return validateResponse( lead, leadFromNationalRegistry );
    }


    private ValidationResultAgainstNationalRegistryDto validateResponse( final Lead lead,
                                                                         final LeadDto leadFromNationalRegistry )
    {
        if ( Objects.isNull( leadFromNationalRegistry ) || !leadsFromNationalServiceAndInternalServiceAreEqual( lead, leadFromNationalRegistry ) )
        {
            log.warn( "The leadId {} is not a valid prospect because of data inconsistency against national services", lead.getIdNumber() );
            return ValidationResultAgainstNationalRegistryDto.builder()
                                                             .id( leadFromNationalRegistry.getIdNumber() )
                                                             .isValid( false )
                                                             .reason( "There is no data, the data is not updated or there is a mismatch against the National Systems" )
                                                             .build();
        }

        return ValidationResultAgainstNationalRegistryDto.builder()
                                                         .id( leadFromNationalRegistry.getIdNumber() )
                                                         .isValid( true )
                                                         .build();
    }


    private boolean leadsFromNationalServiceAndInternalServiceAreEqual( final Lead lead,
                                                                        final LeadDto leadDto )
    {
        return lead.getIdNumber() == leadDto.getIdNumber() &&
               lead.getFirstName().equals( leadDto.getFirstName() ) &&
               lead.getLastName().equals( leadDto.getLastName() ) &&
               lead.getBirthDate().equals( leadDto.getBirthDate() ) &&
               lead.getEmail().equals( leadDto.getEmail() );
    }
}
