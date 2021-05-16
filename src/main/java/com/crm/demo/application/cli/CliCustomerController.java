package com.crm.demo.application.cli;

import com.crm.demo.domain.LeadValidationResponseDto;
import com.crm.demo.domain.service.LeadValidationService;
import com.crm.demo.domain.service.ValidationService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@AllArgsConstructor
public class CliCustomerController
{
    private final ValidationService leadValidationService;

    public LeadValidationResponseDto validateLead(final Integer leadId){
        return leadValidationService.validateLead( leadId );
    }
}
