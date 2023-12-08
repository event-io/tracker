package org.eventio.resources;

import io.quarkus.qute.CheckedTemplate;
import io.quarkus.qute.TemplateInstance;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Cookie;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.eventio.dto.RouteDTO;
import org.eventio.dto.TrackOperation;
import org.eventio.exceptions.FaviconNotFoundException;
import org.eventio.services.RouteService;
import org.eventio.services.SessionService;

import java.io.File;
import java.util.List;

@Slf4j
@RequestScoped
@Path("/routes")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RouteResource {

    @CheckedTemplate
    public static class Templates {
        public static native TemplateInstance route(String routeUrl, String redirectUrl, long timeout);
    }

    private final RouteService routeService;

    private final SessionService sessionService;

    @Inject
    public RouteResource(RouteService routeService, SessionService sessionService) {
        this.routeService = routeService;
        this.sessionService = sessionService;
    }

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
    public TemplateInstance getRoutePage(@PathParam("id") String routeUrl) {
        return Templates.route(routeUrl, "/tracks/auto?routeUrl=" + routeUrl, 3000);
    }

    @GET
    @Path("/{id}/favicon.ico")
    @Produces("image/x-icon")
    public Uni<File> getFavicon(@PathParam("id") String routeUrl, @CookieParam("session") Cookie sessionCookie) {
        log.info("getFavicon for routeUrl: {}", routeUrl);
        Long sessionId = Long.valueOf(sessionCookie.getValue());
        return routeService.getFavicon(sessionId, routeUrl).onItem().ifNull().failWith(NotFoundException::new);
    } //TODO: capire perché non parte la richiesta della favicon (durante il timeout non la chiama? script errato?)

}
