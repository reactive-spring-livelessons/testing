package com.example.testservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.time.Instant;

@SpringBootApplication
public class TestServiceApplication {

		@Bean
		RouterFunction<ServerResponse> greetings() {
				return RouterFunctions.route(RequestPredicates.GET("/hi"), serverRequest -> ServerResponse.ok().body(Flux.just("Hello, world!"), String.class));
		}

		public static void main(String[] args) {
				SpringApplication.run(TestServiceApplication.class, args);
		}
}

@Service
class PublisherService {

		Flux<String> publish() {
				Flux<String> flux = Flux.<String>generate(sink -> sink.next("Hello @ " + Instant.now().toString()))
					.delayElements(Duration.ofSeconds(1));

				return flux;
		}
}