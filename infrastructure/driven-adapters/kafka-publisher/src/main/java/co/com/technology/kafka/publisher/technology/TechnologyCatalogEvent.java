package co.com.technology.kafka.publisher.technology;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TechnologyCatalogEvent {
    private Long id;
    private String name;
}
