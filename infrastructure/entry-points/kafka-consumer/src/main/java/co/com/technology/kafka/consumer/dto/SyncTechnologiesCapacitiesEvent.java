package co.com.technology.kafka.consumer.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SyncTechnologiesCapacitiesEvent {
    private Long capacityId;
    private List<Long> technologyIds;
}