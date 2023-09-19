package org.eventio.resources;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eventio.dto.RouteDTO;
import org.eventio.services.RouteService;

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

}
