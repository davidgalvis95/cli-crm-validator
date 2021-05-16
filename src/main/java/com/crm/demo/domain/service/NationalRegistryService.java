package com.crm.demo.domain.service;

import com.crm.demo.domain.Lead;
import com.crm.demo.domain.LeadDto;
import com.crm.demo.domain.ValidationResultAgainstNationalRegistryDto;
import com.crm.demo.infrastructure.client.NationalRegistryFeignClient;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Objects;


@Slf4j
@Service
@AllArgsConstructor
public class NationalRegistryService
{
    private final NationalRegistryFeignClient nationalRegistryFeignClient;


    public ValidationResultAgainstNationalRegistryDto validateLeadAgainstNationalRegistry( final Lead lead )
    {
        LeadDto leadFromNationalRegistry;
        try
        {
            leadFromNationalRegistry = nationalRegistryFeignClient.getLeadFromNationalRegistry( lead.getIdNumber() );
        }
        catch ( Exception e )
        {
            log.error( Arrays.toString( e.getStackTrace() ) + "====>" + e.getMessage() );
            log.warn( "ERROR: Failed to get record from national registry for leadId {}", lead.getIdNumber() );
            return ValidationResultAgainstNationalRegistryDto.builder()
                                                             .id( lead.getIdNumber() )
                                                             .isValid( false )
                                                             .reason( "There was not possible to get the record from National Systems" )
                                                             .build();
        }



        if ( Objects.isNull( leadFromNationalRegistry ) || !leadsFromNationalServiceAndInternalServiceAreEqual( lead, leadFromNationalRegistry ) )
        {
            log.warn( "The leadId {} is not a valid prospect because of data inconsistency against national services", lead.getIdNumber() );
            return ValidationResultAgainstNationalRegistryDto.builder()
                                                             .id( leadFromNationalRegistry.getIdNumber() )
                                                             .isValid( false )
                                                             .reason( "The data is not updated or there is a mismatch against the National Systems" )
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
