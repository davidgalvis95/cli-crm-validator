package com.crm.demo.infrastructure.config;

import com.crm.demo.infrastructure.client.JudicialRegistryClient;
import com.crm.demo.infrastructure.client.NationalRegistryFeignClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Feign;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class FeignClientConfig
{
    private final String nationalRegistryUrl;

    private final String judicialServiceUrl;


    public FeignClientConfig( @Value( "${national.registry.url}" ) final String nationalRegistryUrl,
                              @Value( "${judicial.registry.url}" ) final String judicialServiceUrl )
    {
        this.nationalRegistryUrl = nationalRegistryUrl;
        this.judicialServiceUrl = judicialServiceUrl;
    }


    @Bean
    public NationalRegistryFeignClient nationalRegistryFeignClient( final ObjectMapper objectMapper )
    {
        log.info( "Creating NationalRegistryFeignClient" );
        final JacksonDecoder jacksonDecoder = new JacksonDecoder( objectMapper );
        return Feign.builder()
                    .encoder( new JacksonEncoder( objectMapper ) )
                    .decoder( jacksonDecoder )
                    .target( NationalRegistryFeignClient.class, nationalRegistryUrl );
    }


    @Bean
    public JudicialRegistryClient judicialRegistryClient( final ObjectMapper objectMapper )
    {
        log.info( "Creating JudicialRegistryClient" );
        final JacksonDecoder jacksonDecoder = new JacksonDecoder( objectMapper );
        return Feign.builder()
                    .encoder( new JacksonEncoder( objectMapper ) )
                    .decoder( jacksonDecoder )
                    .target( JudicialRegistryClient.class, judicialServiceUrl );
    }
}
