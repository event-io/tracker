package org.eventio.mapper;

import org.eventio.dto.SessionDTO;
import org.eventio.entities.Session;
import org.eventio.entities.Track;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.CDI)
public interface SessionMapper {

    @Mapping(target = "track", expression = "java(map(session.getTrack()))")
    @Mapping(target = "id", expression = "java(session.getId())")
    @Mapping(target = "visited", expression = "java(session.getVisited())")
    @Mapping(target = "target", expression = "java(session.getTarget())")
    @Mapping(target = "operation", expression = "java(session.getOperation())")
    SessionDTO map(Session session);

    default Long map(Track track) {
        return track == null ? null : track.getId();
    }

}
