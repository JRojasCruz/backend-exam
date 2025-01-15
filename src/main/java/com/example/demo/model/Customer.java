package com.example.demo.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Setter
@Getter
@Table("customers")
public class Customer {
    @Id
    private Long id;

    private String name;

    private String email;

}
