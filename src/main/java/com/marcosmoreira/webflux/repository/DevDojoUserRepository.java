package com.marcosmoreira.webflux.repository;

import com.marcosmoreira.webflux.domain.DevDojoUserDomain;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface DevDojoUserRepository extends ReactiveCrudRepository<DevDojoUserDomain, Integer> {

    Mono<DevDojoUserDomain> findByUsername(String username);
}