package com.crm.demo.application.controller.cli;

import com.crm.demo.domain.LeadValidationResponseDto;
import com.crm.demo.domain.service.ValidationService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;


@Component
@AllArgsConstructor
public class CliLeadController
{
    private final ValidationService leadValidationService;


    public LeadValidationResponseDto validateLead( final Integer leadId,
                                                   final boolean isaSampleLead )
    {
        return leadValidationService.validateLead( leadId, isaSampleLead );
    }
}
