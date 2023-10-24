package org.eventio.mapper;

import org.eventio.dto.SessionDTO;
import org.eventio.entities.Session;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.CDI)
public interface SessionMapper {

    @Mapping(target = "track", source = "track.id")
    SessionDTO map(Session session);

}
