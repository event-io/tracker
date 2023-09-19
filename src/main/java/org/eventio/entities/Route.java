package org.eventio.entities;

import io.quarkus.hibernate.reactive.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@NamedQueries({
    @NamedQuery(name = "Route.maxIdentifier", query = "SELECT sum(r.bitPosition) FROM Route r"),
    @NamedQuery(name = "Route.getRoutesByTrack", query = "FROM Route r WHERE mod(?1 / r.bitPosition, 2) = 1")
})
public class Route extends PanacheEntity {

    @Column(name = "bit_position", nullable = false, unique = true)
    private int bitPosition;

    @Column(nullable = false, unique = true)
    private String url;

}
