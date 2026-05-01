CREATE TABLE technology_capacities (
    id                    BIGINT AUTO_INCREMENT PRIMARY KEY,
    technology_id         BIGINT NOT NULL,
    capacity_external_id  BIGINT NOT NULL,
    CONSTRAINT fk_technology FOREIGN KEY (technology_id) REFERENCES technologies(id),
    INDEX idx_technology_id (technology_id),
    INDEX idx_capacity_external_id (capacity_external_id),
    UNIQUE INDEX idx_tech_capacity (technology_id, capacity_external_id)
);