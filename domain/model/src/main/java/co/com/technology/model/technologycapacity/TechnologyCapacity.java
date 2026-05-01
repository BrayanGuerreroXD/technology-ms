package co.com.technology.model.technologycapacity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@Builder(toBuilder = true)
public class TechnologyCapacity {
    private Long id;
    private Long technologyId;
    private Long capacityExternalId;
}