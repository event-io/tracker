package org.eventio.dto;

import lombok.Builder;

@Builder
public record SessionDTO(Long id, Long track, Long visited, String target, TrackOperation operation) {}
