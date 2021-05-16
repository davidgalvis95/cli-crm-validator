package com.crm.demo.domain.service;

import com.crm.demo.domain.LeadValidationResponseDto;

import java.util.concurrent.ExecutionException;


public interface ValidationService
{
    LeadValidationResponseDto validateLead( Integer leadId,
                                            boolean isASampleLead );
}
