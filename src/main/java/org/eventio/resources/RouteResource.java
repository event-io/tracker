package org.eventio.resources;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Cookie;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.Response;
import org.eventio.dto.RouteDTO;
import org.eventio.dto.TrackOperation;
import org.eventio.services.RouteService;
import org.eventio.services.SessionService;

import java.io.File;
import java.util.List;
import java.util.UUID;

@RequestScoped
@Path("/routes")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RouteResource {

    @Inject
    RouteService routeService;

    @Inject
    SessionService sessionService;

    @GET
    public Uni<List<RouteDTO>> getRoutesByTrack(@QueryParam("trackId") Long trackId) {
        return trackId == null ? routeService.getAll() : routeService.getByTrackId(trackId);
    }

    @GET
    @Path("/{id}")
    public Uni<RouteDTO> getRoute(@PathParam("id") String id) {
        return routeService.get(id);
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.TEXT_HTML)
    public Uni<Response> getRoutePage(@PathParam("id") String id, @CookieParam("session") Cookie sessionCookie) {
        return routeService.get(id).map(route -> {
            Response.ResponseBuilder response = Response.ok("<html><body><h1>Route: " + route.getUrl() + "</h1></body></html>");
            if (sessionCookie == null || sessionCookie.getValue() == null) {
                var cookie = new NewCookie.Builder("session").sameSite(NewCookie.SameSite.STRICT).value(UUID.randomUUID().toString()).build();
                response = response.cookie(cookie);
            }
            return response.build();
        });
    }

    @GET
    @Path("/{id}/favicon.ico")
    @Produces("image/x-icon")
    public Uni<File> getFavicon(@PathParam("id") String routeUrl, @HeaderParam("x-track-operation") TrackOperation operation, @CookieParam("session") Cookie sessionCookie) {
        return routeService.getFavicon(routeUrl, operation).onItemOrFailure().call(() -> sessionService.addVisited(Long.valueOf(sessionCookie.getValue()), routeUrl));
    }

}
