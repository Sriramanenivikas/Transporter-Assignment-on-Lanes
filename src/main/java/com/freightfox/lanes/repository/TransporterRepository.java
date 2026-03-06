package com.freightfox.lanes.repository;

import com.freightfox.lanes.dto.LaneDto;
import com.freightfox.lanes.dto.TransporterDto;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;


@Slf4j
@Repository
@Getter
public class TransporterRepository {

    private volatile List<LaneDto> lanes = Collections.emptyList();
    private volatile List<TransporterDto> transporters = Collections.emptyList();

    public void saveAll(List<LaneDto> lanes, List<TransporterDto> transporters) {
        this.lanes = Collections.unmodifiableList(lanes);
        this.transporters = Collections.unmodifiableList(transporters);
        log.info("Saved {} lanes and {} transporters", lanes.size(), transporters.size());
    }

    public boolean isDataLoaded() {
        return !lanes.isEmpty() && !transporters.isEmpty();
    }
}
