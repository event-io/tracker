package org.eventio.services;

import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.quarkus.logging.Log;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import org.eventio.dto.RouteDTO;
import org.eventio.dto.TrackOperation;
import org.eventio.entities.Route;
import org.eventio.exceptions.FaviconNotFoundException;

import java.io.File;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class RouteService {

    Uni<Route> ensureRoutes(long id) {
        return Route.count("#Route.maxIdentifier")
                .chain(sum -> {
                    Log.infof("sum: %d", sum);
                    if (id > sum) {
                        return Route.builder()
                                .bitPosition(sum.intValue()+1)
                                .url(UUID.randomUUID().toString())
                                .build()
                                .persistAndFlush();
                    }
                    return Uni.createFrom().nullItem();
                });
    }

    @WithTransaction
    public Uni<List<RouteDTO>> getAll() {
        return Route.findAll().project(RouteDTO.class).list();
    }

    @WithTransaction
    public Uni<List<RouteDTO>> getByTrackId(Long id) {
        return Route.find("#Route.getRoutesByTrack", id).project(RouteDTO.class).list();
    }

    @WithTransaction
    public Uni<RouteDTO> get(String id) {
        return Route.find("url", id).project(RouteDTO.class).firstResult();
    }

    public Uni<File> getFavicon(String id, TrackOperation operation) throws FaviconNotFoundException {
        if (TrackOperation.READ.equals(operation))
            throw new FaviconNotFoundException("Favicon not found for read operation");
        File file = new File("src/main/resources/META-INF/resources/favicon.ico");
        return Uni.createFrom().item(file);
    }
}
