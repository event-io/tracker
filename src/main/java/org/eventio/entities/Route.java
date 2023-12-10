package org.eventio.entities;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Builder
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Entity
@NamedQueries({
    @NamedQuery(name = "Route.maxIdentifier", query = "SELECT sum(r.bitPosition) FROM Route r"),
    @NamedQuery(name = "Route.getRoutesByTrack", query = "FROM Route r WHERE mod(?1 / r.bitPosition, 2) = 1")
})
public class Route {

    @Id
    @GeneratedValue
    private Long id;

    @Column(name = "bit_position", nullable = false, unique = true)
    private int bitPosition;

    @Column(nullable = false, unique = true)
    private String url;

}
