package com.crm.demo;

import com.crm.demo.application.controller.cli.CliLeadController;
import com.crm.demo.domain.LeadValidationResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import java.util.Optional;
import java.util.Scanner;
import java.util.regex.Pattern;


@Slf4j
@EnableMongoRepositories
@EnableFeignClients
@SpringBootApplication
public class CrmApplication
      implements CommandLineRunner
{
    private final ConfigurableApplicationContext context;

    private final CliLeadController customerController;


    @Autowired
    public CrmApplication( ConfigurableApplicationContext context,
                           final CliLeadController customerController )
    {
        this.context = context;
        this.customerController = customerController;
    }


    public static void main( String[] args )
    {
        log.info( "Starting the CRM Validator" );
        SpringApplication.run( CrmApplication.class, args );
        log.info( "Finishing the CRM Validator" );
    }


    @Override
    public void run( String... args )
    {
        try
        {
            Scanner scanner = new Scanner( System.in );
            operateApplication( scanner );
        }catch ( IllegalStateException e ){
            System.err.println("INFO: Closing application due to inactivity");
        }
    }


    public void operateApplication( final Scanner scanner )
    {
        System.out.print( "======================\nMENU OPTIONS\n======================\n\n" );
        System.out.println( "1. Press 'n' to execute a new search" );
        System.out.println( "2. Press 'e' to exit and close the application" );
        final String input = scanner.next();

        if ( input.equals( "n" ) )
        {
            validateCustomer( scanner );
        }
        else if ( input.equals( "e" ) )
        {
            context.close();
        }
        else
        {
            System.err.print( "\nERROR: Input '" + input + "', is not a valid option, please select a valid option\n\n" );
            operateApplication( scanner );
        }
    }


    private void validateCustomer( final Scanner scanner )
    {
        do
        {
            System.out.print( "Please Enter the id of the customer you want to validate (9 digits)\n" );
            final String input = scanner.next();
            System.out.print( "Is this a sample lead (yes/no)\n" );
            final String sampleLead = scanner.next();
            final Pattern digitPattern = Pattern.compile( "\\d{9}" );
            if ( digitPattern.matcher( input ).matches() )
            {
                if ( sampleLead.equals( "yes" ) || sampleLead.equals( "no" ) )
                {
                    processRequest( input, sampleLead.equals( "yes" ) );
                }
                else
                {
                    System.err.print( "\nERROR: Input: '" + sampleLead + "', is not a valid option, please select a valid option. " );
                    validateCustomer( scanner );
                }
            }
            else
            {
                System.err.print( "\nERROR: Input: '" + input + "', is not a valid option, please select a valid option. " );
            }
        }
        while ( continueValidating( scanner ) );

        operateApplication( scanner );
    }


    private void processRequest( final String input,
                                 final boolean isASampleLead )
    {
        final int id = Integer.parseInt( input );
        System.out.print( "INFO: The customer will be validated based on the id: " + id + "\n" );
        LeadValidationResponseDto response = customerController.validateLead( id, isASampleLead );
        System.out.print( "INFO: Validation completed fot id: " + id + "\n\n" );
        System.out.println( "======================\nVALIDATION RESULT\n======================" );
        System.out.println( "Id: " + response.getLead().getIdNumber().toString() );
        System.out.println( "Name: " + response.getLead().getFirstName() + " " + response.getLead().getLastName() );
        System.out.println( "Birthdate: : " + response.getLead().getBirthDate().toString() );
        System.out.println( "Email: " + response.getLead().getEmail() );
        System.out.println( "Score: " + Optional.ofNullable( response.getScore() ).map( s -> s.toString() ).orElse( null ) );
        System.out.println( "The lead is a prospect?: " + response.getIsAProspect().toString().toUpperCase() );
        System.out.println( "Reason: " + response.getReasonMessage() );
    }


    private boolean continueValidating( Scanner scanner )
    {

        while ( true )
        {
            System.out.println( "\nDo you want to validate again? (yes/no)" );
            final String continueValidating = scanner.next();
            if ( continueValidating.equals( "yes" ) || continueValidating.equals( "no" ) )
            {
                return continueValidating.equals( "yes" );
            }
            else
            {
                System.err.print( "\nERROR: Input '" + continueValidating + "', is not a valid option, please select a valid option." );
            }
        }
    }
}
