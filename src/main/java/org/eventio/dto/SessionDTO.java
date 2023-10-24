package org.eventio.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SessionDTO {

    private Long id;

    private Long track;

    private Long visited;

    private String target;

    private TrackOperation operation;

}
