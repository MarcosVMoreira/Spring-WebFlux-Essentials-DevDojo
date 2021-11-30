package com.marcosmoreira.webflux.integration;

import com.marcosmoreira.webflux.domain.AnimeDomain;
import com.marcosmoreira.webflux.repository.AnimeRepository;
import com.marcosmoreira.webflux.service.AnimeService;
import com.marcosmoreira.webflux.util.AnimeCreator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.blockhound.BlockHound;
import reactor.blockhound.BlockingOperationError;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

@ExtendWith(SpringExtension.class)
@WebFluxTest
@Import(AnimeService.class)
public class AnimeControllerIT {
    @MockBean
    private AnimeRepository animeRepository;

    @Autowired
    private WebTestClient testClient;

    private final AnimeDomain animeDomain = AnimeCreator.createValidAnime();

    @BeforeAll
    public static void blockHound() {
        BlockHound.install();
    }

    @Test
    public void blockHoundWorks() {
        try {
            FutureTask<?> task = new FutureTask<>(() -> {
                Thread.sleep(0);
                return "";
            });
            Schedulers.parallel().schedule(task);

            task.get(10, TimeUnit.SECONDS);
            Assertions.fail("should fail");
        } catch (Exception e) {
            Assertions.assertTrue(e.getCause() instanceof BlockingOperationError);
        }
    }

    @BeforeEach
    public void setup() {
        BDDMockito.when(animeRepository.findAll())
                .thenReturn(Flux.just(animeDomain));
    }

    @Test
    @DisplayName("findAll returns a flux of anime")
    public void findAll_ReturnFluxOfAnime_WhenSuccessful() {
        testClient.get()
                .uri("/animes")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.[0].id").isEqualTo(animeDomain.getId())
                .jsonPath("$.[0].name").isEqualTo(animeDomain.getName());
    }

    @Test //má prática. Nao fazer testes de integracao assim
    public void findAll_ReturnFluxOfAnime_WhenSuccessful_OutraManeira() {
        testClient.get()
                .uri("/animes")
                .exchange()
                .expectBodyList(AnimeDomain.class)
                .hasSize(1)
                .contains(animeDomain);
    }
}
