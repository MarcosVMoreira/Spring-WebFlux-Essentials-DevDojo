package com.marcosmoreira.webflux.service;

import com.marcosmoreira.webflux.domain.AnimeDomain;
import com.marcosmoreira.webflux.repository.AnimeRepository;
import io.netty.util.internal.StringUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AnimeService {

    private final AnimeRepository animeRepository;

    public Flux<AnimeDomain> findAll() {
        return animeRepository.findAll();
    }

    public Mono<AnimeDomain> findById(int id) {
        return animeRepository.findById(id)
                .switchIfEmpty(monoResponseStatusNotFoundException());
    }

    public <T> Mono<T> monoResponseStatusNotFoundException() {
        return Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Anime not found"));
    }

    public Mono<AnimeDomain> save(AnimeDomain animeDomain) {
        return animeRepository.save(animeDomain);
    }

    public Mono<Void> update(AnimeDomain animeDomain, int id) {
        return findById(id)
                .map(animeFound -> animeDomain.withId(animeFound.getId()))
                .flatMap(animeRepository::save)
                .then();
    }

    public Mono<Void> delete(int id) {
        return findById(id)
                .flatMap(animeRepository::delete);
    }

    @Transactional
    public Flux<AnimeDomain> saveAll(List<AnimeDomain> animeDomain) {
        return animeRepository.saveAll(animeDomain)
                .doOnNext(this::throwResponseStatusExceptionWhenEmptyName);
    }

    private void throwResponseStatusExceptionWhenEmptyName(AnimeDomain animeDomain) {
        if (StringUtil.isNullOrEmpty(animeDomain.getName()))  {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid name");
        }
    }
}