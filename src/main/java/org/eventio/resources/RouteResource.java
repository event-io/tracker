package org.eventio.resources;

import io.quarkus.qute.CheckedTemplate;
import io.quarkus.qute.TemplateInstance;
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

    @CheckedTemplate
    public static class Templates {
        public static native Uni<TemplateInstance> route(String routeUrl, String redirectUrl, long timeout);
    }

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
    public Uni<TemplateInstance> getRoutePage(@PathParam("id") String routeUrl) {
        return Templates.route(routeUrl, "/tracks/auto", 100);
    }

    @GET
    @Path("/{id}/favicon.ico")
    @Produces("image/x-icon")
    public Uni<File> getFavicon(@PathParam("id") String routeUrl, @HeaderParam("operation") TrackOperation operation, @CookieParam("session") Cookie sessionCookie) {
        return routeService.getFavicon(routeUrl, operation).onItemOrFailure().call(() -> sessionService.addVisited(Long.valueOf(sessionCookie.getValue()), routeUrl));
    }

    private Response.ResponseBuilder addRedirectHeaders(Response.ResponseBuilder builder) {
        return null;
    }

}
