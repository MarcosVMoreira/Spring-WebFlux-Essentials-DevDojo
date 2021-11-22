package com.marcosmoreira.webflux.repository;

import com.marcosmoreira.webflux.domain.AnimeDomain;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface AnimeRepository extends ReactiveCrudRepository<AnimeDomain, Integer> {

    Mono<AnimeDomain> findById(int id);
}