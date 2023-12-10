package org.eventio.dto;

import lombok.*;

@Builder
public record RouteDTO(Long id, int bitPosition, String url) {}
