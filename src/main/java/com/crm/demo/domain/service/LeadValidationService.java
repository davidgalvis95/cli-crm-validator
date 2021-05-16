package com.crm.demo.domain.service;

import com.crm.demo.domain.Lead;
import com.crm.demo.domain.LeadValidationResponseDto;
import com.crm.demo.infrastructure.config.MockServerConfig;
import com.crm.demo.infrastructure.config.WiremockExternalStubbing;
import com.crm.demo.infrastructure.repository.LeadRepository;
import com.crm.demo.infrastructure.repository.ScoreRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.jeasy.random.EasyRandom;
import org.mockserver.integration.ClientAndServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.mockserver.integration.ClientAndServer.startClientAndServer;


@Slf4j
@Component
public class LeadValidationService
      implements ValidationService
{
    private static final EasyRandom GENERATOR = new EasyRandom();

    private LeadRepository leadRepository;

    private ScoreRepository scoreRepository;

    private NationalRegistryService nationalRegistryService;

    private JudicialService judicialService;

    HttpClient client = HttpClientBuilder.create().build();


    @Autowired
    public LeadValidationService( final LeadRepository leadRepository,
                                  final ScoreRepository scoreRepository,
                                  final NationalRegistryService nationalRegistryService,
                                  final JudicialService judicialService )
    {
        this.leadRepository = leadRepository;
        this.scoreRepository = scoreRepository;
        this.nationalRegistryService = nationalRegistryService;
        this.judicialService = judicialService;
    }


    @Override
    public LeadValidationResponseDto validateLead( final Integer leadId )
    {
        Lead lead = leadRepository.findByIdNumber( leadId );

        if ( Objects.isNull( lead ) )
        {
            lead = createNewRandomLead( leadId );
        }

        List<Boolean> validationResults = new ArrayList<>();

        //        TODO: investigate which is the bug in this code
        //        WiremockExternalStubbing stubs = new WiremockExternalStubbing();
        //        stubs.setUp()
        //             .stubJudicialRegistryResponse( lead.getIdNumber() )
        //             .stubNationalRegistryResponse( lead )
        //             .status();

        MockServerConfig mockServerConfig = new MockServerConfig();
        final ClientAndServer server = startClientAndServer( 9000 );
        mockServerConfig.stubNationalRegistryResponse( lead, server );
        mockServerConfig.stubJudicialRegistryResponse( leadId, server );

        try
        {
            final List<CompletableFuture<Boolean>> validationsFromExternalSources = Arrays.asList( leadFromNationalRegistrySystemMatchesInternalDB( lead ), leadHasNoJudicialRecords( lead ) );
            validationResults = CompletableFuture.allOf( validationsFromExternalSources.get( 0 ), validationsFromExternalSources.get( 0 ) )
                                                 .thenApply( future -> validationsFromExternalSources.stream()
                                                                                                     .map( CompletableFuture::join )
                                                                                                     .collect( Collectors.toList() ) )
                                                 .toCompletableFuture().get();
        }
        catch ( InterruptedException | ExecutionException e )
        {
            System.out.println( "There was an error processing customer with id: " + leadId );
            log.warn( "There was an error processing customer with id: {}", e.getMessage() );
            server.stop();
        }

        if ( validationResults.size() == 2 && validationResults.get( 0 ) && validationResults.get( 1 ) )
        {
            final LeadValidationResponseDto.LeadValidationResponseDtoBuilder result = LeadValidationResponseDto.builder().lead( lead );
            final int score = scoreRepository.findScoreByLeadId();

            if ( score < 60 )
            {
                server.stop();
                return result.score( score )
                             .isAProspect( false )
                             .reasonMessage( "The score of the lead is below the accepted limit" )
                             .build();
            }

            server.stop();
            return result.score( score )
                         .isAProspect( true )
                         .reasonMessage( "The lead complies with the requested criteria" )
                         .build();
        }

        return LeadValidationResponseDto.builder()
                                        .lead( lead )
                                        .score( null )
                                        .isAProspect( false )
                                        .reasonMessage( "Either data of the lead does not match national registry, or is reported in judicial registries" )
                                        .build();
    }


    @Async
    public CompletableFuture<Boolean> leadFromNationalRegistrySystemMatchesInternalDB( final Lead lead )
    {
//        return CompletableFuture.supplyAsync( () -> nationalRegistryService.validateLeadAgainstNationalRegistry( lead ).getIsValid() );
        String url = "http://127.0.0.1:9000//api/v1/national-registry/"+ lead.getIdNumber();
        org.apache.http.HttpResponse response=null;
        HttpGet get = new HttpGet(url);
        try {
            response=client.execute(get);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return CompletableFuture.supplyAsync( () -> true );
    }


    @Async
    public CompletableFuture<Boolean> leadHasNoJudicialRecords( final Lead lead )
    {
//        return CompletableFuture.supplyAsync( () -> judicialService.validateIfLeadHasAnyJudicialRecord( lead.getIdNumber() ) );
                String url2 = "http://127.0.0.1:9000//api/v1/judicial-registry/"+ lead.getIdNumber();
                org.apache.http.HttpResponse response2=null;
                HttpGet get2 = new HttpGet(url2);
                try {
                    response2=client.execute(get2);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
        return CompletableFuture.supplyAsync( () -> true );
    }


    private Lead createNewRandomLead( final Integer leadId )
    {
        final Lead newLead = Lead.builder()
                                 .idNumber( leadId )
                                 .firstName( GENERATOR.nextObject( String.class ) )
                                 .lastName( GENERATOR.nextObject( String.class ) )
                                 .birthDate( GENERATOR.nextObject( LocalDate.class ) )
                                 .email( GENERATOR.nextObject( String.class ) + "@addi.com" )
                                 .build();

        return leadRepository.save( newLead );
    }
}
