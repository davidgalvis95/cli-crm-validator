package com.crm.demo.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;


@Data
@Builder
@ToString
@AllArgsConstructor
@Document( collection = "leads" )
public class Lead
{
    @Id
    private Integer idNumber;

    private LocalDate birthDate;

    private String firstName;

    private String lastName;

    private String email;
}
