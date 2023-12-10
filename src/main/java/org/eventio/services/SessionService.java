package org.eventio.services;

import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.eventio.dto.RouteDTO;
import org.eventio.dto.SessionDTO;
import org.eventio.dto.TrackOperation;
import org.eventio.entities.Session;
import org.eventio.mapper.SessionMapper;
import org.eventio.repository.RouteRepository;
import org.eventio.repository.SessionRepository;

import java.util.Comparator;

@Slf4j
@ApplicationScoped
public class SessionService {

    private final SessionMapper sessionMapper;

    private final RouteService routeService;

    private final RouteRepository routeRepository;

    private final SessionRepository sessionRepository;

    @Inject
    public SessionService(SessionMapper sessionMapper, RouteService routeService, RouteRepository routeRepository, SessionRepository sessionRepository) {
        this.sessionMapper = sessionMapper;
        this.routeService = routeService;
        this.routeRepository = routeRepository;
        this.sessionRepository = sessionRepository;
    }

    public Uni<SessionDTO> get(Long id) {
        return sessionRepository.findById(id).map(sessionMapper::map);
    }

    public Uni<SessionDTO> create(String target, TrackOperation operation) {
        Session session = Session.builder()
                .target(target)
                .operation(operation)
                .build();

        return sessionRepository.persistAndFlush(session).onFailure().invoke(Throwable::printStackTrace).map(sessionMapper::map);
    }

    public Uni<RouteDTO> nextRoute(Long trackId, String routeUrl) {
        if (routeUrl == null) {
            return routeRepository.find("bitPosition", 1).project(RouteDTO.class).firstResult();
        }

        if (trackId == null) {
            return routeService.get(routeUrl).flatMap(route -> {
                var bitPosition = getNextBitValue(route.bitPosition());
                return routeRepository.find("bitPosition", bitPosition).project(RouteDTO.class).firstResult();
            });
        }

        return routeService.getByTrackId(trackId).map(list -> {
            RouteDTO currentRoute = list.stream().filter(route -> route.url().equals(routeUrl))
                    .findFirst().orElse(routeService.get(routeUrl).await().indefinitely());
            return list.stream()
                    .filter(route -> route.bitPosition() > currentRoute.bitPosition())
                    .min(Comparator.comparingInt(RouteDTO::bitPosition)).orElse(null);
        });
    }

    public Uni<Session> addVisited(Long sessionId, String routeUrl) {
        return Uni.combine().all().unis(
                sessionRepository.findById(sessionId),
                routeService.get(routeUrl)
        ).withUni((session, route) -> {
            log.info("session: {}, route: {}", session, route);
            if (session.getVisited() < route.bitPosition()) {
                session.setVisited(session.getVisited() + route.bitPosition());
                return sessionRepository.persistAndFlush(session).onItem().invoke(session1 -> log.info("Saved: {}", session1)).onFailure().invoke(Throwable::printStackTrace);
            }
            return Uni.createFrom().item(session);
        });
    }

    private long getNextBitValue(long previousBitValue) {
        long exp = (long) (Math.log(previousBitValue) / Math.log(2));
        return (long) Math.pow(2, exp + 1.0);
    }



}
