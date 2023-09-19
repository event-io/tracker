package org.eventio.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.Set;

@Getter
@Builder
@AllArgsConstructor
public class WriteResponseDTO {

    private Long id;

    private Set<String> routes;

}
