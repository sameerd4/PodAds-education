package com.podads.domain.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Budget {
    private Integer total; // in cents
    private Integer remaining; // in cents
}


