package org.eventio.resources;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.UriInfo;
import org.eventio.dto.RouteDTO;
import org.eventio.dto.TrackDTO;
import org.eventio.dto.TrackOperation;
import org.eventio.dto.WriteResponseDTO;
import org.eventio.services.RouteService;
import org.eventio.services.TrackService;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Path("/tracks")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class TrackResource {

    @Inject
    TrackService trackService;

    @Inject
    RouteService routeService;

    @Context
    UriInfo uriInfo;

    @POST
    public Uni<WriteResponseDTO> writeIdentification() {
        return trackService.write(uriInfo.getRequestUri().toString(), uriInfo.getRequestUri().getHost());
    }

//    @GET
//    public Multi<TrackDTO> getAll() {
//        return trackService.getAll()
//                .onItem().transformToMulti(list -> Multi.createFrom().iterable(list));
//    }

    @GET
    @Path("/{id}")
    public Uni<TrackDTO> get(Long id) {
        return trackService.get(id);
    }

    @GET
    public Uni<TrackDTO> readIdentification(@QueryParam("visited-routes") Set<String> visitedRoutes) {
        return trackService.readIdentification(visitedRoutes);
    }

}
