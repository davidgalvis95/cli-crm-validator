package com.crm.demo.infrastructure.client;

import com.crm.demo.domain.ExternalResponses;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;

import java.io.IOException;


public class AbstractClientMock<T extends ExternalResponses>
{
    public T deserializeResponse( final String URL,
                                  final ObjectMapper objectMapper,
                                  final HttpClient client,
                                  final Class<? extends ExternalResponses> clazz )
          throws IOException
    {
        return (T) objectMapper.readValue( EntityUtils.toString( client.execute( new HttpGet( URL ) ).getEntity(), "UTF-8" ), clazz );
    }
}
