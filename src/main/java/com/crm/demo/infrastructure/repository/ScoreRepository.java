package com.crm.demo.infrastructure.repository;

import org.springframework.stereotype.Component;


/**
 * MockRepo that simulates a fetching of score
 **/

@Component
public class ScoreRepository
{
    public Integer findScoreByLeadId()
    {
        return (int) ( Math.random() * 100 );
    }
}
