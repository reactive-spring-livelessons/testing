package com.example.movieservice;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.test.StepVerifier;

import java.time.Duration;

@RunWith(SpringRunner.class)
@SpringBootTest
public class FluxFlixServiceTest {

		private WebTestClient webTestClient;

		@Autowired
		private FluxFlixService fluxFlixService;

		@Autowired
		private ApplicationContext applicationContext;

		@Before
		public void before() throws Exception {
				this.webTestClient = WebTestClient
					.bindToApplicationContext(this.applicationContext)
					.configureClient()
					.baseUrl("http://localhost:8080/")
					.build();
		}

		@Test
		public void getMoviesReturnsOk() {
				this.webTestClient
					.get()
					.uri("/movies")
					.exchange()
					.expectStatus()
					.isOk();
		}

		@Test
		public void eventsTake10() {
				Movie movie = this.fluxFlixService.all().blockFirst();
				StepVerifier.withVirtualTime(() -> this.fluxFlixService.events(movie.getId())
					.take(10)
					.collectList())
					.thenAwait(Duration.ofHours(1))
					.consumeNextWith(list -> Assert.assertEquals(10, list.size()))
					.verifyComplete();
		}
}