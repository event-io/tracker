package org.eventio.services;

import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.quarkus.panache.common.Sort;
import io.quarkus.runtime.util.StringUtil;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Cookie;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.eventio.dto.*;
import org.eventio.entities.Track;
import org.eventio.repository.RouteRepository;
import org.eventio.repository.SessionRepository;
import org.eventio.repository.TrackRepository;

import java.net.URI;
import java.util.Set;
import java.util.stream.Collectors;

import static org.eventio.resources.TrackResource.PATH_ROUTES;

@Slf4j
@ApplicationScoped
public class TrackService {

    private final RouteService routeService;

    private final RouteRepository routeRepository;

    private final SessionRepository sessionRepository;

    private final SessionService sessionService;

    private final TrackRepository trackRepository;

    @Inject
    public TrackService(RouteService routeService, RouteRepository routeRepository, SessionRepository sessionRepository, SessionService sessionService, TrackRepository trackRepository) {
        this.routeService = routeService;
        this.routeRepository = routeRepository;
        this.sessionRepository = sessionRepository;
        this.sessionService = sessionService;
        this.trackRepository = trackRepository;
    }

    public Uni<TrackDTO> createTrack(String user, String ip) {
        return trackRepository.persistAndFlush(Track.builder()
                    .usr(user)
                    .ip(ip)
                    .build()).onFailure().invoke(Throwable::printStackTrace)
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
                .with((complement, max) -> {
                    log.info("complement: {}, max: {}", complement, max);
                    log.info("bit complement: {}, bit max: {}", Integer.toBinaryString(complement), Integer.toBinaryString(max));
                    log.info("~complement: {}, ~complement&max: {}", ~complement, (~complement) & max);
                    log.info("bit ~complement: {}, bit ~complement&max: {}", Integer.toBinaryString(~complement), Integer.toBinaryString((~complement) & max));
                    return (~complement) & max;
                })
                .map(Integer::toUnsignedLong);
    }

    @WithTransaction
    public Uni<Response> auto(TrackOperation operation, String target, String routeUrl, Cookie sessionCookie, Cookie trackCookie, String requestUri, String requestHost) {
        if (trackCookie != null && !StringUtil.isNullOrEmpty(trackCookie.getValue())) {
            return Uni.createFrom().item(Response.temporaryRedirect(URI.create("urlshort/" + target)).build());
        }
        Uni<SessionDTO> sessionDTO;
        if (sessionCookie != null && !StringUtil.isNullOrEmpty(sessionCookie.getValue())) {
            sessionDTO = sessionService.get(Long.valueOf(sessionCookie.getValue()));
        } else {
            sessionDTO = sessionService.create(target, operation);
        }
        return sessionDTO.flatMap(session -> {
            var cookie = new NewCookie.Builder("session").sameSite(NewCookie.SameSite.STRICT).value(session.id().toString()).httpOnly(true).path("/").build();
            //TODO: if WRITE set track to null
            Uni<RouteDTO> nextRoute = sessionService.nextRoute(null, routeUrl);
            // check if routeDTO is null
            return nextRoute.onItem().ifNotNull().transform(route -> Response.temporaryRedirect(URI.create(PATH_ROUTES + route.url())).cookie(cookie).build())
                    .onItem().ifNull().switchTo(sessionFinished(session, cookie, requestUri, requestHost));
        });
    }

    private Uni<Response> sessionFinished(SessionDTO session, NewCookie sessionCookie, String requestUri, String requestHost) {
        if (TrackOperation.READ.equals(session.operation())) {
            Uni<Long> trackId = readIdentification(Uni.createFrom().item(session.visited().intValue()));
            return trackId.flatMap(id -> {
                if (id == 0) {
                    Uni<Track> trackUni = trackRepository.persistAndFlush(Track.builder()
                            .usr(requestUri)
                            .ip(requestHost)
                            .build()).onFailure().invoke(Throwable::printStackTrace);
                    return trackUni.flatMap(track -> {
                        log.info("track created: {}", track);
                        var trackCookie = new NewCookie.Builder("track").sameSite(NewCookie.SameSite.STRICT).value(track.getId().toString()).httpOnly(true).path("/").build();
                        return sessionRepository.findById(session.id()).flatMap(sessionEntity -> {
                            sessionEntity.setTrack(track);
                            sessionEntity.setVisited(0);
                            sessionEntity.setOperation(TrackOperation.WRITE);
                            return sessionRepository.persistAndFlush(sessionEntity).onFailure().invoke(Throwable::printStackTrace).chain(() ->sessionService.nextRoute(null, null)
                                    .map(route -> Response.temporaryRedirect(URI.create(PATH_ROUTES + route.url())).cookie(trackCookie).cookie(sessionCookie).build())
                            );
                        });
                    });
                } else {
                    return trackRepository.findById(id).flatMap(track -> {
                        log.info("track found: {}", track);
                        // TODO: verificare se il redirect setta il track cokie così da evitare il redirect sull'ultima rotta solo per il set del cookie
                        var trackCookie = new NewCookie.Builder("track").sameSite(NewCookie.SameSite.STRICT).value(id.toString()).httpOnly(true).path("/").build();
                        return routeRepository.findAll(Sort.descending("bitPosition")).firstResult().flatMap(route ->
                                sessionRepository.findById(session.id()).flatMap(sessionEntity -> {
                                    sessionEntity.setTrack(track);
                                    sessionEntity.setVisited(id);
                                    sessionEntity.setOperation(TrackOperation.WRITE);
                                    return sessionRepository.persistAndFlush(sessionEntity).onFailure().invoke(Throwable::printStackTrace).chain(() -> Uni.createFrom().item(Response.temporaryRedirect(URI.create(PATH_ROUTES + route.getUrl())).cookie(trackCookie).cookie(sessionCookie).build()));
                                })
                        );
                    });
                }
            });
        } else {
            return Uni.createFrom().item(Response.temporaryRedirect(URI.create("urlshort/" + session.target())).build());
        }
    }

}
