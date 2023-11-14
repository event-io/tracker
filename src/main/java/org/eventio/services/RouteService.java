package org.eventio.services;

import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.quarkus.logging.Log;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eventio.dto.RouteDTO;
import org.eventio.dto.TrackOperation;
import org.eventio.entities.Route;
import org.eventio.exceptions.FaviconNotFoundException;
import org.eventio.repository.RouteRepository;

import java.io.File;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class RouteService {

    private final RouteRepository routeRepository;

    @Inject
    public RouteService(RouteRepository routeRepository) {
        this.routeRepository = routeRepository;
    }

    Uni<Route> ensureRoutes(long id) {
        return routeRepository.count("#Route.maxIdentifier")
                .chain(sum -> {
                    Log.infof("sum: %d", sum);
                    if (id > sum) {
                        return routeRepository.persistAndFlush(Route.builder()
                                .bitPosition(sum.intValue()+1)
                                .url(UUID.randomUUID().toString())
                                .build());
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

    public Uni<File> getFavicon(String id, TrackOperation operation) {
        if (TrackOperation.READ.equals(operation))
            return Uni.createFrom().failure(new FaviconNotFoundException("Favicon not found for read operation"));
        File file = new File("src/main/resources/META-INF/resources/favicon-alien.ico");
        return Uni.createFrom().item(file);
    }
}
