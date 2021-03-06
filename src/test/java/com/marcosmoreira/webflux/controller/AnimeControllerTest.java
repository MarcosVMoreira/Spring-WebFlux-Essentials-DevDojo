package com.marcosmoreira.webflux.controller;

import com.marcosmoreira.webflux.domain.AnimeDomain;
import com.marcosmoreira.webflux.service.AnimeService;
import com.marcosmoreira.webflux.util.AnimeCreator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.blockhound.BlockHound;
import reactor.blockhound.BlockingOperationError;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import static org.mockito.ArgumentMatchers.anyInt;

@ExtendWith(SpringExtension.class)
class AnimeControllerTest {

    @InjectMocks
    private AnimeController animeController;

    @Mock
    private AnimeService animeService;

    private final AnimeDomain animeDomain = AnimeCreator.createValidAnime();

    @BeforeAll
    public static void blockHound() {
        BlockHound.install();
    }

    @BeforeEach
    public void setup() {
        BDDMockito.when(animeService.findAll())
                .thenReturn(Flux.just(animeDomain));

        BDDMockito.when(animeService.findById(anyInt()))
                .thenReturn(Mono.just(animeDomain));

        BDDMockito.when(animeService.save(AnimeCreator.createAnimeToBeSaved()))
                .thenReturn(Mono.just(animeDomain));

        BDDMockito.when(animeService.delete(anyInt()))
                .thenReturn(Mono.empty());

        BDDMockito.when(animeService.update(AnimeCreator.createValidUpdatedAnime(), 1))
                .thenReturn(Mono.empty());

        BDDMockito.when(animeService.saveAll(List.of(AnimeCreator.createAnimeToBeSaved(),
                AnimeCreator.createAnimeToBeSaved())))
                .thenReturn(Flux.just(animeDomain, animeDomain));
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

    @Test
    @DisplayName("listAll returns a flux of anime")
    public void listAll_ReturnFluxOfAnime_WhenSuccessful() {
        StepVerifier.create(animeController.listAll())
                .expectSubscription()
                .expectNext(animeDomain)
                .verifyComplete();
    }

    @Test
    @DisplayName("findById returns Mono with anime when it exists")
    public void findById_ReturnMonoAnime_WhenSuccessful() {
        StepVerifier.create(animeController.findById(1))
                .expectSubscription()
                .expectNext(animeDomain)
                .verifyComplete();
    }


    @Test
    @DisplayName("save creates an anime when successfull")
    public void save_CreatesAnime_WhenSuccessful(){
        AnimeDomain animeToBeSaved = AnimeCreator.createAnimeToBeSaved();

        StepVerifier.create(animeController.save(animeToBeSaved))
                .expectSubscription()
                .expectNext(animeDomain)
                .verifyComplete();
    }

    @Test
    @DisplayName("delete removes then anime when successful")
    public void delete_RemovesAnime_WhenSuccessful(){
        StepVerifier.create(animeController.delete(1))
                .expectSubscription()
                .verifyComplete();
    }

    @Test
    @DisplayName("update save updated anime and returns empty mono when successful")
    public void update_SaveUpdatedAnime_WhenSuccessful(){
        StepVerifier.create(animeController.update(1, AnimeCreator.createValidUpdatedAnime()))
                .expectSubscription()
                .verifyComplete();
    }

    @Test
    @DisplayName("saveBatch creates a list of anime when successfull")
    public void saveBatch_CreatesListOfAnime_WhenSuccessful(){
        AnimeDomain animeToBeSaved = AnimeCreator.createAnimeToBeSaved();

        StepVerifier.create(animeController.saveBatch(List.of(animeToBeSaved, animeToBeSaved)))
                .expectSubscription()
                .expectNext(animeDomain, animeDomain)
                .verifyComplete();
    }

}