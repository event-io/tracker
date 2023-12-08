package org.eventio.entities;

import jakarta.persistence.*;
import lombok.*;
import org.eventio.dto.TrackOperation;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class Session {

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne(cascade = {CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
    @JoinColumn(name = "track_id")
    private Track track;

    @Builder.Default
    private long visited = 0;

    private String target;

    @Enumerated(EnumType.STRING)
    private TrackOperation operation;

}
