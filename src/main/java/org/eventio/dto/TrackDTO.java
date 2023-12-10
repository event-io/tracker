package org.eventio.dto;

import lombok.*;

@Builder
public record TrackDTO(Long id, String ip, String usr) {}
