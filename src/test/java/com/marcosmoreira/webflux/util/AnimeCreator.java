package com.marcosmoreira.webflux.util;

import com.marcosmoreira.webflux.domain.AnimeDomain;

public class AnimeCreator {

    public static AnimeDomain createAnimeToBeSaved() {
        return AnimeDomain.builder()
                .name("Nome de um anime")
                .build();
    }

    public static AnimeDomain createValidAnime() {
        return AnimeDomain.builder()
                .id(1)
                .name("Nome de um anime")
                .build();
    }

    public static AnimeDomain createValidUpdatedAnime() {
        return AnimeDomain.builder()
                .id(1)
                .name("Nome de um anime 2")
                .build();
    }
}
