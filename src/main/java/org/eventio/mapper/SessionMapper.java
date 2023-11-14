package org.eventio.mapper;

import org.eventio.dto.SessionDTO;
import org.eventio.entities.Session;
import org.eventio.entities.Track;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.CDI)
public interface SessionMapper {

    SessionDTO map(Session session);

    default Long map(Track track) {
        return track.id;
    }

}
