package org.eventio.entities;

import io.quarkus.hibernate.reactive.panache.PanacheEntity;
import jakarta.persistence.*;
import lombok.*;
import org.eventio.dto.TrackOperation;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Session extends PanacheEntity {

    @ManyToOne(cascade = {CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
    @JoinColumn(name = "track_id")
    private Track track;

    private long visited = 0;

    private String target;

    @Enumerated(EnumType.STRING)
    private TrackOperation operation;

}
