package com.nisync.incident.timeline.service;

import com.nisync.incident.timeline.dto.IncidentTimelineItemDto;

import java.util.List;

public interface IncidentTimelineService {

    List<IncidentTimelineItemDto> getTimelineByIncidentId(Long incidentId);
}
