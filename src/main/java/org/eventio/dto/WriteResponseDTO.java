package org.eventio.dto;

import lombok.Builder;

import java.util.Set;

@Builder
public record WriteResponseDTO(Long id, Set<String> routes) {}
