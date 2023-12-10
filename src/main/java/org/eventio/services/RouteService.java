package org.eventio.services;

import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.quarkus.logging.Log;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.eventio.dto.RouteDTO;
import org.eventio.dto.TrackOperation;
import org.eventio.entities.Route;
import org.eventio.exceptions.FaviconNotFoundException;
import org.eventio.repository.RouteRepository;

import java.io.File;
import java.util.List;
import java.util.UUID;

@Slf4j
@ApplicationScoped
public class RouteService {

    private final RouteRepository routeRepository;

    private final SessionService sessionService;

    @Inject
    public RouteService(RouteRepository routeRepository, SessionService sessionService) {
        this.routeRepository = routeRepository;
        this.sessionService = sessionService;
    }

    Uni<Route> ensureRoutes(long id) {
        return routeRepository.count("#Route.maxIdentifier")
                .chain(sum -> {
                    Log.infof("sum: %d", sum);
                    if (id > sum) {
                        return routeRepository.persistAndFlush(Route.builder()
                                .bitPosition(sum.intValue()+1)
                                .url(UUID.randomUUID().toString())
                                .build()).onFailure().invoke(Throwable::printStackTrace);
                    }
                    return Uni.createFrom().nullItem();
                });
    }

    @WithTransaction
    public Uni<List<RouteDTO>> getAll() {
        return routeRepository.findAll().project(RouteDTO.class).list();
    }

    @WithTransaction
    public Uni<List<RouteDTO>> getByTrackId(Long id) {
        return routeRepository.find("#Route.getRoutesByTrack", id).project(RouteDTO.class).list();
    }

    @WithTransaction
    public Uni<RouteDTO> get(String id) {
        return routeRepository.find("url", id).project(RouteDTO.class).firstResult();
    }

    @WithTransaction
    public Uni<File> getFavicon(Long sessionId, String routeUrl) {
        log.info("getFavicon for sessionId: {}, routeUrl: {}", sessionId, routeUrl);
        return sessionService.get(sessionId).flatMap(session -> {
            log.info("operation: {}", session.operation());
            if (TrackOperation.READ.equals(session.operation()))
                return Uni.createFrom().failure(new FaviconNotFoundException("Favicon not found for read operation"));
            File file = new File("src/main/resources/META-INF/resources/favicon-alien.ico");
            return Uni.createFrom().item(file);
        }).onItem()
                .call(() -> sessionService.addVisited(sessionId, routeUrl))
            .onFailure(FaviconNotFoundException.class).recoverWithUni(() ->
                sessionService.addVisited(sessionId, routeUrl).onItem().transform(session -> null)
            );
    }
}
