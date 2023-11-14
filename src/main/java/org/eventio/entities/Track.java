package org.eventio.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
public class Track {

    @Id
    @GeneratedValue
    private Long id;

    private String ip;

    private String usr;

}
