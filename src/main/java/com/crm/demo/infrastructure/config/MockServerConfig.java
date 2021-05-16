package com.crm.demo.infrastructure.config;

import com.crm.demo.domain.Lead;
import lombok.extern.slf4j.Slf4j;
import org.jeasy.random.EasyRandom;
import org.mockserver.client.server.MockServerClient;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.Header;

import java.time.LocalDate;
import java.util.concurrent.TimeUnit;

import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.mockserver.matchers.Times.exactly;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;


@Slf4j
public class MockServerConfig
{
    public ClientAndServer server;

    private static final EasyRandom GENERATOR = new EasyRandom();


    public void setUp()
    {
        server = startClientAndServer( 9000 );
    }


    public void stubNationalRegistryResponse( final Lead lead,
                                              final ClientAndServer server )
    {

        final boolean shouldReturnTheSameObject = ( (int) ( Math.random() * 10 ) ) > 2;
        String jsonLead;

        jsonLead = getJsonLead( lead, shouldReturnTheSameObject );

        new MockServerClient( "127.0.0.1", 9000 )
              .when(
                    request()
                          .withMethod( "GET" )
                          .withPath( "/api/v1/national-registry/" + lead.getIdNumber() )
                          .withHeader( "\"Content-type\", \"application/json\"" ),
                    exactly( 1 ) )
              .respond(
                    response()
                          .withStatusCode( 200 )
                          .withHeaders(
                                new Header( "Content-Type", "application/json; charset=utf-8" ) )
                          .withBody( jsonLead )
                          .withDelay( TimeUnit.SECONDS, 1 ) );

        log.info( "National Registry Service is properly configured" );
    }


    private String getJsonLead( final Lead lead,
                                final boolean shouldReturnTheSameObject )
    {
        String jsonLead;
        if ( shouldReturnTheSameObject )
        {
            jsonLead = "{\"idNumber\":" + lead.getIdNumber() + ",\"" +
                       "birthDate\":\"" + lead.getBirthDate().toString() + "\",\"" +
                       "firstName\":\"" + lead.getFirstName() + "\",\"" +
                       "lastName\":\"" + lead.getLastName() + "\",\"" +
                       "email\":\"" + lead.getEmail() + "\"}";
        }
        else
        {
            jsonLead = "{\"idNumber\":" + lead.getIdNumber() + ",\"" +
                       "birthDate\":\"" + GENERATOR.nextObject( LocalDate.class ).toString() + "\",\"" +
                       "firstName\":\"" + GENERATOR.nextObject( String.class ) + "\",\"" +
                       "lastName\":\"" + GENERATOR.nextObject( String.class ) + "\",\"" +
                       "email\":\"" + GENERATOR.nextObject( String.class ) + "@addi.com\"}";
        }
        return jsonLead;
    }


    public void stubJudicialRegistryResponse( final Integer leadId,
                                              final ClientAndServer server )
    {
        final boolean shouldHaveJudicialRecords = ( (int) ( Math.random() * 10 ) ) < 1;
        String resultJson;

        if ( shouldHaveJudicialRecords )
        {
            resultJson = "{\"id\":" + leadId + ",\"hasJudicialRecords\":true}";
        }
        else
        {
            resultJson = "{\"id\":" + leadId + ",\"hasJudicialRecords\":false}";
        }

        new MockServerClient( "127.0.0.1", 9000 )
              .when(
                    request()
                          .withMethod( "GET" )
                          .withPath( "/api/v1/judicial-registry/" + leadId )
                          .withHeader( "\"Content-type\", \"application/json\"" ),
                    exactly( 1 ) )
              .respond(
                    response()
                          .withStatusCode( 200 )
                          .withHeaders(
                                new Header( "Content-Type", "application/json; charset=utf-8" ) )
                          .withBody( resultJson )
                          .withDelay( TimeUnit.SECONDS, 1 ) );

        log.info( "Judicial Registry Service is properly configured" );
    }
}
