package org.eventio.services;

import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.quarkus.logging.Log;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import org.eventio.dto.RouteDTO;
import org.eventio.entities.Route;

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
                                .persist();
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

}
