package org.eventio.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class TrackDTO {

    private Long id;

    private String ip;

    private String usr;

}
