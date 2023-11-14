package org.eventio.resources;

import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.quarkus.panache.common.Sort;
import io.quarkus.runtime.util.StringUtil;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import org.eventio.dto.*;
import org.eventio.entities.Route;
import org.eventio.entities.Session;
import org.eventio.entities.Track;
import org.eventio.services.RouteService;
import org.eventio.services.SessionService;
import org.eventio.services.TrackService;

import java.net.URI;
import java.util.Set;

@Path("/tracks")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class TrackResource {

    @Inject
    TrackService trackService;

    @Inject
    SessionService sessionService;

    @Inject
    RouteService routeService;

    @Context
    UriInfo uriInfo;

    @POST
    public Uni<WriteResponseDTO> writeIdentification() {
        return trackService.write(uriInfo.getRequestUri().toString(), uriInfo.getRequestUri().getHost());
    }

    @GET
    @Path("/{id}")
    public Uni<TrackDTO> get(Long id) {
        return trackService.get(id);
    }

    @GET
    public Uni<TrackDTO> readIdentification(@QueryParam("visited-routes") Set<String> visitedRoutes) {
        return trackService.readIdentification(visitedRoutes);
    }

    @GET
    @Path("/auto")
    @Produces(MediaType.TEXT_HTML)
    public Uni<Response> auto(@QueryParam("operation") TrackOperation operation, @QueryParam("target") String target, @QueryParam("routeUrl") String routeUrl, @CookieParam("session") Cookie sessionCookie, @CookieParam("track") Cookie trackCookie) {
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
            var cookie = new NewCookie.Builder("session").sameSite(NewCookie.SameSite.STRICT).value(session.getId().toString()).build();
            Uni<RouteDTO> nextRoute = sessionService.nextRoute(null, routeUrl); //TODO: if WRITE set track to null
            // check if routeDTO is null
            return nextRoute.onItem().ifNotNull().transform(route -> Response.temporaryRedirect(URI.create("routes/" + route.getUrl())).cookie(cookie).build())
                    .onItem().ifNull().switchTo(sessionFinished(session, cookie));
        });
    }

    @WithTransaction
    private Uni<Response> sessionFinished(SessionDTO session, NewCookie sessionCookie) {
        if (TrackOperation.READ.equals(session.getOperation())) {
            Uni<Long> trackId = trackService.readIdentification(Uni.createFrom().item(session.getVisited().intValue()));
            return trackId.flatMap(id -> {
                if (id == 0) {
                    Uni<TrackDTO> trackDTO = trackService.createTrack(uriInfo.getRequestUri().toString(), uriInfo.getRequestUri().getHost());
                    return trackDTO.flatMap(dto -> Track.findById(dto.getId())).map(o -> (Track) o).flatMap(track -> {
                        var trackCookie = new NewCookie.Builder("track").sameSite(NewCookie.SameSite.STRICT).value(track.id.toString()).build();
                        return Session.findById(session.getId()).flatMap(s -> {
                            Session sessionEntity = (Session) s;
                            sessionEntity.setTrack(track);
                            sessionEntity.setVisited(0);
                            sessionEntity.setOperation(TrackOperation.WRITE);
                            sessionEntity.persistAndFlush();
                            Uni<RouteDTO> nextRoute = sessionService.nextRoute(null, null);
                            return nextRoute.map(route -> Response.temporaryRedirect(URI.create("routes/" + route.getUrl())).cookie(trackCookie).cookie(sessionCookie).build());
                        });
                    });
                } else {
                    return Track.findById(id).map(o -> (Track) o).flatMap(track -> {
                        // TODO: verificare se il redirect setta il track cokie così da evitare il redirect sull'ultima rotta solo per il set del cookie
                        var trackCookie = new NewCookie.Builder("track").sameSite(NewCookie.SameSite.STRICT).value(id.toString()).build();
                        return Route.findAll(Sort.descending("bitPosition")).firstResult().map(o -> (Route) o).flatMap(route ->
                            Session.findById(session.getId()).map(s -> {
                                Session sessionEntity = (Session) s;
                                sessionEntity.setTrack(track);
                                sessionEntity.setVisited(id);
                                sessionEntity.setOperation(TrackOperation.WRITE);
                                sessionEntity.persistAndFlush();
                                return Response.temporaryRedirect(URI.create("routes/" + route.getUrl())).cookie(trackCookie).cookie(sessionCookie).build();
                            })
                        );
                    });
                }
            });
        } else {
            return Uni.createFrom().item(Response.temporaryRedirect(URI.create("urlshort/" + session.getTarget())).build());
        }
    }

}
