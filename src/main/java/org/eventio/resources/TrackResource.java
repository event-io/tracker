package org.eventio.resources;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.UriInfo;
import org.eventio.dto.TrackDTO;
import org.eventio.dto.WriteResponseDTO;
import org.eventio.services.TrackService;

@Path("/tracks")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class TrackResource {

    @Inject
    TrackService trackService;

    @Context
    UriInfo uriInfo;

    @POST
    public Uni<WriteResponseDTO> writeIdentification() {
        return trackService.createTrack(uriInfo.getRequestUri().toString(), uriInfo.getRequestUri().getHost())
                .onItem().transform(trackDTO -> WriteResponseDTO.builder().id(trackDTO.getId()).build());
    }

    @GET
    public Multi<TrackDTO> getAll() {
        return trackService.getAll()
                .onItem().transformToMulti(list -> Multi.createFrom().iterable(list));
    }

    @GET
    @Path("/{id}")
    public Uni<TrackDTO> get(Long id) {
        return trackService.get(id);
    }

}
