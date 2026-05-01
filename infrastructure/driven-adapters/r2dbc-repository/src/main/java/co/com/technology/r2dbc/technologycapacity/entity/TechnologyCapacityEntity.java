package co.com.technology.r2dbc.technologycapacity.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Index;
import org.springframework.data.relational.core.mapping.Table;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Table("technology_capacities")
public class TechnologyCapacityEntity {
    @Id
    private Long id;

    @Column("technology_id")
    @Index("idx_technology_id")
    private Long technologyId;

    @Column("capacity_external_id")
    @Index("idx_capacity_external_id")
    private Long capacityExternalId;
}