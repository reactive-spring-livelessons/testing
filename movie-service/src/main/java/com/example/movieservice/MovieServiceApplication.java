package com.example.movieservice;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Date;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;

@SpringBootApplication
public class MovieServiceApplication {

		public static void main(String[] args) {
				SpringApplication.run(MovieServiceApplication.class, args);
		}

		@Bean
		ApplicationRunner init(MovieRepository movieRepository) {
				return args -> {
						movieRepository
							.deleteAll()
							.thenMany(
								Flux
									.just("Foo", "Bar")
									.flatMap(title -> movieRepository.save(new Movie(null, title))))
							.thenMany(movieRepository.findAll())
							.subscribe(System.out::println);
				};
		}
}

@Configuration
class WebConfiguration {

		@Bean
		RouterFunction<?> routes(FluxFlixService ffs) {
				return RouterFunctions
					.route(GET("/movies"), serverRequest -> ServerResponse.ok().body(ffs.all(), Movie.class))
					.andRoute(GET("/movies/{id}"), serverRequest -> ServerResponse.ok().body(ffs.byId(serverRequest.pathVariable("id")), Movie.class))
					.andRoute(GET("/movies/{id}/events"), serverRequest -> ServerResponse.ok()
						.contentType(MediaType.TEXT_EVENT_STREAM)
						.body(ffs.events(serverRequest.pathVariable("id")), MovieEvent.class));
		}
}

@Service
class FluxFlixService {

		private final MovieRepository movieRepository;

		FluxFlixService(MovieRepository movieRepository) {
				this.movieRepository = movieRepository;
		}

		public Flux<MovieEvent> events(String movieId) {
				return Flux.<MovieEvent>generate(sink -> sink.next(new MovieEvent(movieId, new Date())))
					.delayElements(Duration.ofSeconds(1));
		}

		public Mono<Movie> byId(String id) {
				return this.movieRepository.findById(id);
		}

		public Flux<Movie> all() {
				return this.movieRepository.findAll();
		}
}

interface MovieRepository extends ReactiveMongoRepository<Movie, String> {
}

@Document
@Data
@NoArgsConstructor
@AllArgsConstructor
class Movie {
		@Id
		private String id;
		private String title;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
class MovieEvent {
		private String movieId;
		private Date date;
}
