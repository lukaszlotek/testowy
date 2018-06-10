package pl.javasurvival.helloServer;

import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.HttpHandler;
import org.springframework.http.server.reactive.ReactorHttpHandlerAdapter;
import org.springframework.web.reactive.function.server.HandlerFunction;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import reactor.ipc.netty.http.server.HttpServer;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.springframework.web.reactive.function.BodyInserters.fromObject;
import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RequestPredicates.path;
import static org.springframework.web.reactive.function.server.RouterFunctions.nest;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

public class HelloServerApplication {
    final private List<Message> messages = new ArrayList<>();


    private HelloServerApplication() {
        messages.add(new Message("test content", "Zenek Testowy"));
        messages.add(new Message("bla bla", "Tester"));
    }


   private AtomicInteger counter = new AtomicInteger(0);

    public static void main(String[] args) {
        new HelloServerApplication().serve();

    }

    private void serve() {
        RouterFunction route = nest(path("/api"),
                route(GET("/time"), renderGetTime())
                        .andRoute(GET("/messages"), renderMessages())
                        .andRoute(POST("/messages"), postMessage()));

        HttpHandler httpHandler = RouterFunctions.toHttpHandler(route);
        HttpServer server = HttpServer.create("localhost", 8080);
        server.startAndAwait(new ReactorHttpHandlerAdapter(httpHandler));
    }

    private HandlerFunction<ServerResponse> postMessage() {
        return request -> {
            Mono<Message> postedMessage = request.bodyToMono(Message.class);
            return postedMessage.flatMap( message -> {
                messages.add(message);
                return ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(fromObject(messages));
            });

        };
    }

    private HandlerFunction<ServerResponse> renderMessages() {
        return request -> {

            return ServerResponse.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(fromObject(messages));
        };
    }

    private HandlerFunction<ServerResponse> renderGetTime() {
        return request -> {
            LocalDateTime now = LocalDateTime.now();
            DateTimeFormatter myFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");

            return ServerResponse.ok()
                    .contentType(MediaType.TEXT_PLAIN)
                    .body(fromObject(myFormatter.format(now)));
        };
    }

}
