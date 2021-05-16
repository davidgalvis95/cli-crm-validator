package com.crm.demo.infrastructure.client;

import com.crm.demo.domain.JudicialRecordsDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.http.client.HttpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;


@Slf4j
@Service
public class JudicialRegistryClientMock
      extends AbstractClientMock<JudicialRecordsDto>
{

    private final String URL;


    @Autowired
    public JudicialRegistryClientMock( final Pair<String, String> externalServiceUrls )
    {
        this.URL = externalServiceUrls.getRight();
    }


    public JudicialRecordsDto getMockedResponseFromJudicialService( final int leadId,
                                                                    final ObjectMapper objectMapper,
                                                                    final HttpClient client )
    {
        JudicialRecordsDto response;
        final String url = URL + leadId;
        try
        {
            response = deserializeResponse( url, objectMapper, client, JudicialRecordsDto.class );
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
