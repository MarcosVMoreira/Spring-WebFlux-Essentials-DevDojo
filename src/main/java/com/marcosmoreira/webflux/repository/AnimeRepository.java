package com.marcosmoreira.webflux.repository;

import com.marcosmoreira.webflux.domain.AnimeDomain;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface AnimeRepository extends ReactiveCrudRepository<AnimeDomain, Integer> {
}
