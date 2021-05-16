package com.crm.demo.domain.service;

import com.crm.demo.domain.Lead;
import com.crm.demo.domain.LeadValidationResponseDto;
import com.crm.demo.domain.ValidationResultAgainstNationalRegistryDto;
import com.crm.demo.infrastructure.config.MockServerConfig;
import com.crm.demo.infrastructure.repository.LeadRepository;
import com.crm.demo.infrastructure.repository.ScoreRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.jeasy.random.EasyRandom;
import org.mockserver.integration.ClientAndServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static org.mockserver.integration.ClientAndServer.startClientAndServer;


@Slf4j
@Component
public class LeadValidationService
      implements ValidationService
{
    private static final EasyRandom GENERATOR = new EasyRandom();

    private static final HttpClient HTTP_CLIENT = HttpClientBuilder.create().build();

    private LeadRepository leadRepository;

    private ScoreRepository scoreRepository;

    private NationalRegistryService nationalRegistryService;

    private JudicialService judicialService;

    private ObjectMapper objectMapper;


    @Autowired
    public LeadValidationService( final LeadRepository leadRepository,
                                  final ScoreRepository scoreRepository,
                                  final NationalRegistryService nationalRegistryService,
                                  final JudicialService judicialService,
                                  final ObjectMapper objectMapper )
    {
        this.leadRepository = leadRepository;
        this.scoreRepository = scoreRepository;
        this.nationalRegistryService = nationalRegistryService;
        this.judicialService = judicialService;
        this.objectMapper = objectMapper;
    }


    @Override
    public LeadValidationResponseDto validateLead( final Integer leadId,
                                                   final boolean isASampleLead )
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
            final CompletableFuture<Boolean> leadMatchesNationalServiceAndInternalOnesFuture = leadFromNationalRegistrySystemMatchesInternalDB( lead, isASampleLead );
            final CompletableFuture<Boolean> leadHasJudicialRecords = leadHasJudicialRecords( lead, isASampleLead );

            final List<CompletableFuture<Boolean>> validationsFromExternalSources = Arrays.asList( leadMatchesNationalServiceAndInternalOnesFuture, leadHasJudicialRecords );
            validationResults = CompletableFuture.allOf( leadMatchesNationalServiceAndInternalOnesFuture, leadHasJudicialRecords )
                                                 .thenApply( future -> validationsFromExternalSources.stream()
                                                                                                     .map( CompletableFuture::join )
                                                                                                     .collect( Collectors.toList() ) )
                                                 .toCompletableFuture().get();

            if ( leadHasJudicialRecords.isCompletedExceptionally() || leadMatchesNationalServiceAndInternalOnesFuture.isCompletedExceptionally() )
            {
                server.stop();
                log.warn( "Processing completed with exceptions for lead id {}", leadId );
                return LeadValidationResponseDto.builder()
                                                .lead( lead )
                                                .score( null )
                                                .isAProspect( false )
                                                .reasonMessage( "Failed to get information from external systems" )
                                                .build();
            }
        }
        catch ( InterruptedException | ExecutionException e )
        {
            System.out.println( "There was an error processing customer with id: " + leadId );
            log.warn( "There was an error processing lead with id: {}", e.getMessage() );
            server.stop();
            return LeadValidationResponseDto.builder()
                                            .lead( lead )
                                            .score( null )
                                            .isAProspect( false )
                                            .reasonMessage( "Failed to process the validation" )
                                            .build();
        }

        if ( validationResults.size() == 2 && validationResults.get( 0 ) && !validationResults.get( 1 ) )
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

        server.stop();
        return LeadValidationResponseDto.builder()
                                        .lead( lead )
                                        .score( null )
                                        .isAProspect( false )
                                        .reasonMessage( "Either data of the lead does not match national registry, or is reported in judicial registries" )
                                        .build();
    }


    @Async
    public CompletableFuture<Boolean> leadFromNationalRegistrySystemMatchesInternalDB( final Lead lead,
                                                                                       final Boolean isASampleLead )
    {
        return CompletableFuture.supplyAsync( () -> {
            if ( isASampleLead )
            {
                final ValidationResultAgainstNationalRegistryDto response = nationalRegistryService.validateSampleLeadAgainstNationalRegistry( lead, objectMapper, HTTP_CLIENT );
                return Objects.nonNull( response ) && response.getIsValid();
            }
            final ValidationResultAgainstNationalRegistryDto response = nationalRegistryService.validateLeadAgainstNationalRegistry( lead );
            return Objects.nonNull( response ) && response.getIsValid();
        } );
    }


    @Async
    public CompletableFuture<Boolean> leadHasJudicialRecords( final Lead lead,
                                                              final Boolean isASampleLead )
    {
        return CompletableFuture.supplyAsync( () -> {
            if ( isASampleLead )
            {
                return judicialService.validateIfSampleLeadHasAnyJudicialRecord( lead.getIdNumber(), objectMapper, HTTP_CLIENT );
            }
            return judicialService.validateIfLeadHasAnyJudicialRecord( lead.getIdNumber() );
        } );
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
