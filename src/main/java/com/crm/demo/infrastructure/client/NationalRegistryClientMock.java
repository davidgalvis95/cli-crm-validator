package com.crm.demo.infrastructure.client;

import com.crm.demo.domain.LeadDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.http.client.HttpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;


@Slf4j
@Service
public class NationalRegistryClientMock
      extends AbstractClientMock<LeadDto>
{
    private final String URL;


    @Autowired
    public NationalRegistryClientMock( final Pair<String, String> externalServiceUrls )
    {
        this.URL = externalServiceUrls.getLeft();
    }


    public LeadDto getMockedResponseFromNationalService( final int leadId,
                                                         final ObjectMapper objectMapper,
                                                         final HttpClient client )
    {
        LeadDto response = null;
        final String url = URL + leadId;
        try
        {
            response = deserializeResponse( url, objectMapper, client, LeadDto.class );
        }
        catch ( IOException e )
        {
            log.error( e.getMessage() );
            throw new RuntimeException( e );
        }
        log.info( response.toString() );
        return response;
    }
}
