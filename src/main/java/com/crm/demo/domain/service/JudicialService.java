package com.crm.demo.domain.service;

import com.crm.demo.domain.JudicialRecordsDto;
import com.crm.demo.infrastructure.client.JudicialRegistryClient;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Optional;


@Slf4j
@Service
@AllArgsConstructor
public class JudicialService
{
    private final JudicialRegistryClient judicialRegistryClient;


    public Boolean validateIfLeadHasAnyJudicialRecord( final int leadId )
    {

        JudicialRecordsDto judicialRecords = null;
        try
        {
            judicialRecords = judicialRegistryClient.getJudicialRecordsByLeadId( leadId );
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
