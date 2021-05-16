package com.crm.demo.infrastructure.repository;

import com.crm.demo.domain.Lead;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface LeadRepository
      extends MongoRepository<Lead, Integer>
{
    Lead findByIdNumber( final Integer id );
}
