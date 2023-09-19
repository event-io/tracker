package org.eventio.services;

import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eventio.dto.TrackDTO;
import org.eventio.entities.Track;

import java.util.List;

@ApplicationScoped
public class TrackService {

    @Inject
    RouteService routeService;

    @WithTransaction
    public Uni<TrackDTO> createTrack(String user, String ip) {
        return Track.builder()
                    .usr(user)
                    .ip(ip)
                    .build()
                .persist()
                .chain(track -> this.get(((Track) track).id))
                .call(trackDTO -> routeService.ensureRoutes(trackDTO.getId()));
    }

    @WithTransaction
    public Uni<List<TrackDTO>> getAll() {
        return Track.findAll().project(TrackDTO.class).list();
    }

    @WithTransaction
    public Uni<TrackDTO> get(Long id) {
        return Track.find("id", id).project(TrackDTO.class).firstResult();
    }

}
