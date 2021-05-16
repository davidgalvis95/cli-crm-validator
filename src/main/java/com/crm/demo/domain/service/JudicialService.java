package com.crm.demo.domain.service;

import com.crm.demo.domain.JudicialRecordsDto;
import com.crm.demo.infrastructure.client.JudicialRegistryClient;
import com.crm.demo.infrastructure.config.MockServerConfig;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mockserver.integration.ClientAndServer;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Optional;

import static org.mockserver.integration.ClientAndServer.startClientAndServer;


@Slf4j
@Service
@AllArgsConstructor
public class JudicialService
{
    private final JudicialRegistryClient judicialRegistryClient;

    private static final String URL = "http://127.0.0.1:9000/api/v1/judicial-registry/";


    public Boolean validateIfLeadHasAnyJudicialRecord( final int leadId )
    {

        JudicialRecordsDto judicialRecords = null;
        try
        {
//            MockServerConfig mockServerConfig = new MockServerConfig();
//            final ClientAndServer server = startClientAndServer( 9000 );
////            mockServerConfig.setUp();
//            mockServerConfig.stubJudicialRegistryResponse( leadId, server );
//            judicialRecords = judicialRegistryClient.getJudicialRecordsByLeadId( leadId );
            judicialRecords = (JudicialRecordsDto) ThirdPartyLeadQueryService.stubbedResponseJudicialSystems( leadId, URL, false );
        }
        catch ( Exception e )
        {
            log.error( Arrays.toString( e.getStackTrace() ) + "====>" + e.getMessage()  );
            log.warn( "ERROR: Failed to get data from judicial registry => {}", e.getMessage() );
        }

        return Optional.ofNullable( judicialRecords )
                       .map( JudicialRecordsDto::getHasJudicialRecords )
                       .orElseGet( () -> {
                           log.warn( "No data is available for the id {} in judicial systems, the lead is not a valid prospect", leadId );
                           return true;
                       } );
    }
}
