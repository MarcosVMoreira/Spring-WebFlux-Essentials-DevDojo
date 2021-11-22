package com.marcosmoreira.webflux.service;

import com.marcosmoreira.webflux.domain.AnimeDomain;
import com.marcosmoreira.webflux.repository.AnimeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class AnimeService {

    private final AnimeRepository animeRepository;

    public Flux<AnimeDomain> findAll() {
        return animeRepository.findAll();
    }

    public Mono<AnimeDomain> findById(int id) {
        return animeRepository.findById(id)
                .switchIfEmpty(monoResponseStatusNotFoundException())
                .log();
    }

    public <T> Mono<T> monoResponseStatusNotFoundException() {
        return Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Anime not found"));
    }
}