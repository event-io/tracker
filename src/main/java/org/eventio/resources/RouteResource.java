package org.eventio.resources;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eventio.dto.RouteDTO;
import org.eventio.dto.TrackOperation;
import org.eventio.exceptions.FaviconNotFoundException;
import org.eventio.services.RouteService;

import java.io.File;
import java.util.List;

@RequestScoped
@Path("/routes")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RouteResource {

    @Inject
    RouteService routeService;

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
    public Uni<Response> getRoutePage(@PathParam("id") String id) {
        return routeService.get(id).map(route -> Response.ok("<html><body><h1>Route: " + route.getUrl() + "</h1></body></html>").build());
    }

    @GET
    @Path("/{id}/favicon.ico")
    @Produces("image/x-icon")
    public Uni<File> getFavicon(@PathParam("id") String id, @HeaderParam("x-track-operation") TrackOperation operation) {
        try {
            return routeService.getFavicon(id, operation);
        } catch (FaviconNotFoundException e) {
            throw new NotFoundException(e.getMessage(), e);
        }
    }

}
