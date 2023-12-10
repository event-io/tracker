package org.eventio.repository;

import io.quarkus.hibernate.reactive.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.eventio.entities.Route;

@ApplicationScoped
public class RouteRepository implements PanacheRepository<Route> {
}
