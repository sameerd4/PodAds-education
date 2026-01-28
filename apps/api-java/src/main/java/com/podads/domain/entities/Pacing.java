package com.podads.domain.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Pacing {
    private Integer dailyBudget; // in cents
    private Integer dailySpend; // in cents
}


