package com.crm.demo.infrastructure.config;

import com.crm.demo.domain.Lead;
import com.github.tomakehurst.wiremock.WireMockServer;
import lombok.extern.slf4j.Slf4j;
import org.jeasy.random.EasyRandom;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;


@Slf4j
@Component
public class WiremockExternalStubbing
{
    private static final EasyRandom GENERATOR = new EasyRandom();

    public WireMockServer server;


    public WiremockExternalStubbing setUp()
    {
        server = new WireMockServer( 8000 );
        server.start();
        log.info( "Wiremock has started in the following url: {}", "http://localhost:8000\n\n" );
        return this;
    }


    public WiremockExternalStubbing resetServer()
    {
        server.resetAll();
        return this;
    }


    public WiremockExternalStubbing stubNationalRegistryResponse( final Lead lead )
    {
        final boolean shouldReturnTheSameObject = ( (int) ( Math.random() * 10 ) ) > 2;
        String jsonLead;

        jsonLead = getJsonlead( lead, shouldReturnTheSameObject );

        stubFor( get( urlPathEqualTo( "/api/v1/national-registry/" + lead.getIdNumber() ) )
                       .willReturn( aResponse().withStatus( 200 )
                                               .withHeader( "Content-Type", "application/json" )
                                               .withBody( jsonLead ) ) );


        log.info( "National Registry Service is properly configured" );
        return this;
    }


    private String getJsonlead( final Lead lead,
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


    public WiremockExternalStubbing stubJudicialRegistryResponse( final Integer leadId )
    {
        final boolean shouldHaveJudicialRecords = ( (int) ( Math.random() * 10 ) ) > 1;
        String resultJson;

        if ( shouldHaveJudicialRecords )
        {
            resultJson = "{\"id\":" + leadId + ",\"hasJudicialRecords\":true}";
        }
        else
        {
            resultJson = "{\"id\":" + leadId + ",\"hasJudicialRecords\":false}";
        }



        stubFor( get( urlPathEqualTo( "/api/v1/judicial-registry/" + leadId ) )
                       .willReturn( aResponse().withStatus( 200 )
                                               .withHeader( "Content-Type", "application/json" )
                                               .withBody( resultJson ) ) );

        log.info( "Judicial Registry Service is properly configured" );
        return this;
    }


    public WiremockExternalStubbing status()
    {
        System.out.println( "Stubbing Started!" );
        return this;
    }
}
