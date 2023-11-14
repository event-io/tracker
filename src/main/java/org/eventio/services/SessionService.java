package org.eventio.services;

import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eventio.dto.RouteDTO;
import org.eventio.dto.SessionDTO;
import org.eventio.dto.TrackOperation;
import org.eventio.entities.Route;
import org.eventio.entities.Session;
import org.eventio.mapper.SessionMapper;

import java.util.Comparator;

@ApplicationScoped
public class SessionService {

    @Inject
    SessionMapper sessionMapper;

    @Inject
    RouteService routeService;

    @Inject
    TrackService trackService;

    public Uni<SessionDTO> get(Long id) {
        return Session.findById(id).map(s -> sessionMapper.map((Session) s));
    }

    @WithTransaction
    public Uni<SessionDTO> create(String target, TrackOperation operation) {
        Session session = Session.builder()
                .target(target)
                .operation(operation)
                .build();

        return session.persistAndFlush().map(s -> sessionMapper.map((Session) s));
    }

    public Uni<RouteDTO> nextRoute(Long trackId, String routeUrl) {
        if (routeUrl == null) {
            return Route.find("bitPosition", 1).project(RouteDTO.class).firstResult();
        }

        if (trackId == null) {
            return routeService.get(routeUrl).flatMap(route -> {
                var bitPosition = getNextBitValue(route.getBitPosition());
                return Route.find("bitPosition", bitPosition).project(RouteDTO.class).firstResult();
            });
        }

        return routeService.getByTrackId(trackId).map(list -> {
            RouteDTO currentRoute = list.stream().filter(route -> route.getUrl().equals(routeUrl))
                    .findFirst().orElse(routeService.get(routeUrl).await().indefinitely());
            return list.stream()
                    .filter(route -> route.getBitPosition() > currentRoute.getBitPosition())
                    .min(Comparator.comparingInt(RouteDTO::getBitPosition)).orElse(null);
        });
    }

    @WithTransaction
    public Uni<Session> addVisited(Long sessionId, String routeUrl) {
        return Uni.combine().all().unis(
                Session.findById(sessionId).map(o -> (Session) o),
                routeService.get(routeUrl)
        ).combinedWith((session, route) -> {
            session.setVisited(session.getVisited() + route.getBitPosition());
            session.persistAndFlush();
            return session;
        });
    }

    private long getNextBitValue(long previousBitValue) {
        long exp = (long) (Math.log(previousBitValue) / Math.log(2));
        return (long) Math.pow(2, exp + 1);
    }



}
