package org.eventio.resources;

import io.quarkus.panache.common.Sort;
import io.quarkus.runtime.util.StringUtil;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import org.eventio.dto.*;
import org.eventio.repository.RouteRepository;
import org.eventio.repository.SessionRepository;
import org.eventio.repository.TrackRepository;
import org.eventio.services.SessionService;
import org.eventio.services.TrackService;

import java.net.URI;
import java.util.Set;

@Path("/tracks")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class TrackResource {

    public static final String PATH_ROUTES = "routes/";
    private final TrackService trackService;

    private final SessionService sessionService;

    private final RouteRepository routeRepository;

    private final SessionRepository sessionRepository;

    private final TrackRepository trackRepository;

    @Context
    UriInfo uriInfo;

    @Inject
    public TrackResource(TrackService trackService, SessionService sessionService, RouteRepository routeRepository, SessionRepository sessionRepository, TrackRepository trackRepository) {
        this.trackService = trackService;
        this.sessionService = sessionService;
        this.routeRepository = routeRepository;
        this.sessionRepository = sessionRepository;
        this.trackRepository = trackRepository;
    }

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
        return trackService.auto(operation, target, routeUrl, sessionCookie, trackCookie, uriInfo.getRequestUri().toString(), uriInfo.getRequestUri().getHost());
    }

}
