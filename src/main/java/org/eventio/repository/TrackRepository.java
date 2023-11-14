package org.eventio.repository;

import io.quarkus.hibernate.reactive.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.eventio.entities.Track;

@ApplicationScoped
public class TrackRepository implements PanacheRepository<Track> {
}
