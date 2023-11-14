package org.eventio.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
public record SessionDTO(Long id, Long track, Long visited, String target, TrackOperation operation) {}
