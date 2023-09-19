package org.eventio.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class RouteDTO {

    private Long id;

    private int bitPosition;

    private String url;

}
