package com.crm.demo.domain.service;

import com.crm.demo.domain.ExternalResponses;
import com.crm.demo.domain.JudicialRecordsDto;
import com.crm.demo.domain.LeadDto;
import com.crm.demo.infrastructure.config.MockServerConfig;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.mockserver.integration.ClientAndServer;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static org.mockserver.integration.ClientAndServer.startClientAndServer;


@Slf4j
@AllArgsConstructor
public class ThirdPartyLeadQueryService
{
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public static ExternalResponses stubbedResponseJudicialSystems( final int leadId,
                                                                    final String url,
                                                                    boolean isNationalSystem )
    {
        final String dynamicUrl = url + leadId;
        final HttpClient client = HttpClientBuilder.create().build();
        HttpResponse response;
        final AtomicReference<LeadDto> mappedResponseN = new AtomicReference<>();
        final AtomicReference<JudicialRecordsDto> mappedResponseJ = new AtomicReference<>();

        final HttpGet get = new HttpGet( dynamicUrl );

        try
        {
            response = client.execute( get );
            Optional.ofNullable( response ).ifPresent( r -> {
                try
                {
                    if(isNationalSystem){
                        mappedResponseN.set( OBJECT_MAPPER.readValue( EntityUtils.toString( r.getEntity(), "UTF-8" ), LeadDto.class ) );
                    }else {
                        mappedResponseJ.set( OBJECT_MAPPER.readValue( EntityUtils.toString( r.getEntity(), "UTF-8" ), JudicialRecordsDto.class ) );
                    }
                }
                catch ( IOException e )
                {
                    e.printStackTrace();
                }
            } );
        }
        catch ( IOException e )
        {
            throw new RuntimeException( e );
        }
        return isNationalSystem ? mappedResponseN.get() : mappedResponseJ.get();
    }
}
