package com.crm.demo.infrastructure.exception;

public class ExternalPublicServiceProcessingException
      extends RuntimeException
{
    public ExternalPublicServiceProcessingException( String message )
    {
        super( message );
    }
}
