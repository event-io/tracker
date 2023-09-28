package org.eventio.services;

import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eventio.dto.RouteDTO;
import org.eventio.dto.TrackDTO;
import org.eventio.dto.WriteResponseDTO;
import org.eventio.entities.Route;
import org.eventio.entities.Track;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
                .persistAndFlush()
                .chain(track -> this.get(((Track) track).id))
                .call(trackDTO -> routeService.ensureRoutes(trackDTO.getId()));
    }

    @WithTransaction
    public Uni<WriteResponseDTO> write(String user, String ip) {
        Uni<TrackDTO> trackDTO = createTrack(user, ip);
        return trackDTO.chain(track -> {
            WriteResponseDTO.WriteResponseDTOBuilder builder = WriteResponseDTO.builder().id(track.getId());
            return routeService.getByTrackId(track.getId()).map(routes -> builder.routes(routes.stream().map(RouteDTO::getUrl).collect(Collectors.toSet())).build());
        });
    }

    @WithTransaction
    public Uni<List<TrackDTO>> getAll() {
        return Track.findAll().project(TrackDTO.class).list();
    }

    @WithTransaction
    public Uni<TrackDTO> get(Long id) {
        return Track.find("id", id).project(TrackDTO.class).firstResult();
    }

    @WithTransaction
    public Uni<TrackDTO> readIdentification(Set<String> visitedRoutes) {
        Uni<Integer> complementary = Multi.createFrom().iterable(visitedRoutes)
                .onItem().transformToUniAndMerge(route -> Route.find("url", route).project(RouteDTO.class).firstResult().map(RouteDTO::getBitPosition))
                .collect().asList()
                .map(list -> list.stream().mapToInt(Integer::intValue).sum());

        Uni<Integer> maxIdentifier = Route.count("#Route.maxIdentifier").map(Long::intValue);
        return Uni.combine().all().unis(complementary, maxIdentifier)
                .combinedWith((complement, max) -> (~complement)&max)
                .map(Integer::toUnsignedLong)
                .chain(this::get);
    }

}
