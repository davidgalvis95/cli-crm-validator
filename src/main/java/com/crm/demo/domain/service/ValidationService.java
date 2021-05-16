package com.crm.demo.domain.service;

import com.crm.demo.domain.Lead;
import com.crm.demo.domain.LeadValidationResponseDto;


public interface ValidationService
{
    LeadValidationResponseDto validateLead( Integer leadId );
}
