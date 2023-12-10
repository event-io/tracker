package org.eventio.repository;

import io.quarkus.hibernate.reactive.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.eventio.entities.Session;

@ApplicationScoped
public class SessionRepository implements PanacheRepository<Session> {
}
