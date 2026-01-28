package com.podads.domain.services;

import com.podads.domain.entities.AdRequest;
import com.podads.domain.entities.CandidateAd;
import com.podads.domain.valueobjects.FilterResult;

public interface Filter {
    String getName();
    FilterResult apply(AdRequest request, CandidateAd candidate, int randomSeed);
}


