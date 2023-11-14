package org.eventio.services;

import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eventio.dto.RouteDTO;
import org.eventio.dto.TrackDTO;
import org.eventio.dto.WriteResponseDTO;
import org.eventio.entities.Track;
import org.eventio.repository.RouteRepository;
import org.eventio.repository.TrackRepository;

import java.util.Set;
import java.util.stream.Collectors;

@ApplicationScoped
public class TrackService {

    private final RouteService routeService;

    private final RouteRepository routeRepository;

    private final TrackRepository trackRepository;

    @Inject
    public TrackService(RouteService routeService, RouteRepository routeRepository, TrackRepository trackRepository) {
        this.routeService = routeService;
        this.routeRepository = routeRepository;
        this.trackRepository = trackRepository;
    }

    @WithTransaction
    public Uni<TrackDTO> createTrack(String user, String ip) {
        return trackRepository.persistAndFlush(Track.builder()
                    .usr(user)
                    .ip(ip)
                    .build())
                .chain(track -> this.get(track.getId()))
                .call(trackDTO -> routeService.ensureRoutes(trackDTO.id()));
    }

    @WithTransaction
    public Uni<WriteResponseDTO> write(String user, String ip) {
        Uni<TrackDTO> trackDTO = createTrack(user, ip);
        return trackDTO.chain(track -> {
            WriteResponseDTO.WriteResponseDTOBuilder builder = WriteResponseDTO.builder().id(track.id());
            return routeService.getByTrackId(track.id()).map(routes -> builder.routes(routes.stream().map(RouteDTO::url).collect(Collectors.toSet())).build());
        });
    }

    @WithTransaction
    public Uni<TrackDTO> get(Long id) {
        return trackRepository.find("id", id).project(TrackDTO.class).firstResult();
    }

    @WithTransaction
    public Uni<TrackDTO> readIdentification(Set<String> visitedRoutes) {
        Uni<Integer> complementary = Multi.createFrom().iterable(visitedRoutes)
                .onItem().transformToUniAndMerge(route -> routeRepository.find("url", route).project(RouteDTO.class).firstResult().map(RouteDTO::bitPosition))
                .collect().asList()
                .map(list -> list.stream().mapToInt(Integer::intValue).sum());

        return readIdentification(complementary).chain(this::get);
    }

    public Uni<Long> readIdentification(Uni<Integer> complementary) {
        Uni<Integer> maxIdentifier = routeRepository.count("#Route.maxIdentifier").map(Long::intValue);
        return Uni.combine().all().unis(complementary, maxIdentifier)
                .with((complement, max) -> (~complement)&max)
                .map(Integer::toUnsignedLong);
    }

}
