package com.crm.demo.domain.service;

import com.crm.demo.domain.Lead;
import com.crm.demo.domain.LeadDto;
import com.crm.demo.domain.ValidationResultAgainstNationalRegistryDto;
import com.crm.demo.infrastructure.client.NationalRegistryFeignClient;
import com.crm.demo.infrastructure.config.MockServerConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mockserver.integration.ClientAndServer;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Objects;

import static org.mockserver.integration.ClientAndServer.startClientAndServer;


@Slf4j
@Service
@AllArgsConstructor
public class NationalRegistryService
{
    private final NationalRegistryFeignClient nationalRegistryFeignClient;

    private static final String URL = "http://127.0.0.1:9000/api/v1/national-registry/";


    public ValidationResultAgainstNationalRegistryDto validateLeadAgainstNationalRegistry( final Lead lead )
    {
        LeadDto leadFromNationalRegistry;
        try
        {
//            MockServerConfig mockServerConfig = new MockServerConfig();
//            final ClientAndServer server = startClientAndServer( 9000 );
//            //            mockServerConfig.setUp();
//            mockServerConfig.stubJudicialRegistryResponse( lead.getIdNumber(), server );
//            //            leadFromNationalRegistry = nationalRegistryFeignClient.getLeadFromNationalRegistry( lead.getIdNumber() );
            leadFromNationalRegistry = (LeadDto) ThirdPartyLeadQueryService.stubbedResponseJudicialSystems( lead.getIdNumber(), URL, true );
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
