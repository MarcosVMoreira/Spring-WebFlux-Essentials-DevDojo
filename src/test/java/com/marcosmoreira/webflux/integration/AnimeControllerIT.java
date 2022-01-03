package com.marcosmoreira.webflux.integration;

import com.marcosmoreira.webflux.domain.AnimeDomain;
import com.marcosmoreira.webflux.repository.AnimeRepository;
import com.marcosmoreira.webflux.util.AnimeCreator;
import com.marcosmoreira.webflux.util.WebTestClientUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import reactor.blockhound.BlockHound;
import reactor.blockhound.BlockingOperationError;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyIterable;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureWebTestClient
public class AnimeControllerIT {
    @MockBean
    private AnimeRepository animeRepository;

    @Autowired
    private WebTestClientUtil webTestClientUtil;

    private WebTestClient testClientUser;
    private WebTestClient testClientAdmin;
    private WebTestClient testClientInvalid;

    private final AnimeDomain animeDomain = AnimeCreator.createValidAnime();

    @BeforeAll
    public static void blockHound() {
        BlockHound.install(
                builder -> builder.allowBlockingCallsInside("java.util.UUID", "randomUUID")
        );
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
        testClientUser = webTestClientUtil.authenticateClient("moreira", "devdojo");
        testClientAdmin = webTestClientUtil.authenticateClient("marcos", "devdojo");
        testClientInvalid = webTestClientUtil.authenticateClient("x", "x");

        BDDMockito.when(animeRepository.findAll())
                .thenReturn(Flux.just(animeDomain));

        BDDMockito.when(animeRepository.findById(anyInt()))
                .thenReturn(Mono.just(animeDomain));

        BDDMockito.when(animeRepository.save(AnimeCreator.createAnimeToBeSaved()))
                .thenReturn(Mono.just(animeDomain));

        BDDMockito.when(animeRepository.delete(any(AnimeDomain.class)))
                .thenReturn(Mono.empty());

        BDDMockito.when(animeRepository.save(AnimeCreator.createValidUpdatedAnime()))
                .thenReturn(Mono.empty());

        BDDMockito.when(animeRepository.saveAll(List.of(AnimeCreator.createAnimeToBeSaved(),
                        AnimeCreator.createAnimeToBeSaved())))
                .thenReturn(Flux.just(animeDomain, animeDomain));
    }

    @Test
    @DisplayName("findAll returns forbidden when user is successfully authenticated and does not have role ADMIN")
    public void findAll_ReturnsForbbiden_WhenUserDoesNotHaveRoleAdmin() {
        testClientUser.get()
                .uri("/animes")
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    @DisplayName("findAll returns a flux of anime when user is successfully authenticated and has role ADMIN")
    public void findAll_ReturnFluxOfAnime_WhenSuccessful() {
        testClientAdmin.get()
                .uri("/animes")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.[0].id").isEqualTo(animeDomain.getId())
                .jsonPath("$.[0].name").isEqualTo(animeDomain.getName());
    }

    @Test //má prática. Nao fazer testes de integracao assim
    public void findAll_ReturnFluxOfAnime_WhenSuccessful_OutraManeira() {
        testClientAdmin.get()
                .uri("/animes")
                .exchange()
                .expectBodyList(AnimeDomain.class)
                .hasSize(1)
                .contains(animeDomain);
    }

    @Test
    @DisplayName("findById returns Mono with anime when it exists")
    public void findById_ReturnMonoAnime_WhenSuccessful() {
        testClientUser.get()
                .uri("/animes/{id}", 1)
                .exchange()
                .expectStatus().isOk()
                .expectBody(AnimeDomain.class)
                .isEqualTo(animeDomain);
    }

    @Test
    @DisplayName("findById returns Mono error when anime does not exist")
    public void findById_ReturnMonoError_WhenEmptyMonoIsReturned() {
        BDDMockito.when(animeRepository.findById(anyInt()))
                .thenReturn(Mono.empty());

        testClientUser.get()
                .uri("/animes/{id}", 1)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.status").isEqualTo(404)
                .jsonPath("$.developerMessage").isEqualTo("A ResponseStatusException Happened");
    }

    @Test
    @DisplayName("save creates an anime when successful and user is successfully authenticated and has role ADMIN")
    public void save_CreatesAnime_WhenSuccessful(){
        AnimeDomain animeToBeSaved = AnimeCreator.createAnimeToBeSaved();

        testClientAdmin.post()
                .uri("/animes/")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(animeToBeSaved))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(AnimeDomain.class)
                .isEqualTo(animeDomain);
    }

    @Test
    @DisplayName("saveBatch creates a list of anime when successful and user is successfully authenticated and has role ADMIN")
    public void saveBatch_CreatesListOfAnime_WhenSuccessful(){
        AnimeDomain animeToBeSaved = AnimeCreator.createAnimeToBeSaved();

        testClientAdmin.post()
                .uri("/animes/batch")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(List.of(animeToBeSaved, animeToBeSaved)))
                .exchange()
                .expectStatus().isCreated()
                .expectBodyList(AnimeDomain.class)
                .hasSize(2)
                .contains(animeDomain);
    }

    @Test
    @DisplayName("save returns mono error with bad request when name is empty and user is successfully authenticated and has role ADMIN")
    public void save_ReturnsError_WhenNameIsEmpty(){
        AnimeDomain animeToBeSaved = AnimeCreator.createAnimeToBeSaved().withName("");

        testClientAdmin.post()
                .uri("/animes/")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(animeToBeSaved))
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.status").isEqualTo(400);
    }

    @Test
    @DisplayName("saveBatch returns Mono error when one of the objects in the list contains null or empty name  and user is successfully authenticated and has role ADMIN")
    public void saveBatch_ReturnsMonoError_WhenContainsInvalidName(){
        AnimeDomain animeToBeSaved = AnimeCreator.createAnimeToBeSaved().withName("");

        BDDMockito.when(animeRepository.saveAll(anyIterable()))
                .thenReturn(Flux.just(animeDomain, animeDomain.withName("")));

        testClientAdmin.post()
                .uri("/animes/batch")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(List.of(animeToBeSaved, animeToBeSaved)))
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.status").isEqualTo(400);
    }


    @Test
    @DisplayName("delete removes the anime when successful and user is successfully authenticated and has role ADMIN")
    public void delete_RemovesAnime_WhenSuccessful(){
        testClientAdmin.delete()
                .uri("/animes/{id}", 1)
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    @DisplayName("delete returns Mono error when anime does not exist and user is successfully authenticated and has role ADMIN")
    public void delete_ReturnMonoError_WhenEmptyMonoIsReturned(){
        BDDMockito.when(animeRepository.findById(anyInt()))
                .thenReturn(Mono.empty());

        testClientAdmin.delete()
                .uri("/animes/{id}", 1)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.status").isEqualTo(404)
                .jsonPath("$.developerMessage").isEqualTo("A ResponseStatusException Happened");
    }

    @Test
    @DisplayName("update save updated anime and returns empty mono when successful and user is successfully authenticated and has role ADMIN")
    public void update_SaveUpdatedAnime_WhenSuccessful(){
        BDDMockito.when(animeRepository.findById(anyInt()))
                .thenReturn(Mono.just(AnimeCreator.createAnimeToBeSaved()));

        testClientAdmin.put()
                .uri("/animes/{id}", 1)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(animeDomain))
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    @DisplayName("update returns Mono error when anime does not exist and user is successfully authenticated and has role ADMIN")
    public void update_ReturnMonoError_WhenEmptyMonoIsReturned(){
        BDDMockito.when(animeRepository.findById(anyInt()))
                .thenReturn(Mono.empty());

        testClientAdmin.put()
                .uri("/animes/{id}", 1)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(animeDomain))
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.status").isEqualTo(404)
                .jsonPath("$.developerMessage").isEqualTo("A ResponseStatusException Happened");
    }
}