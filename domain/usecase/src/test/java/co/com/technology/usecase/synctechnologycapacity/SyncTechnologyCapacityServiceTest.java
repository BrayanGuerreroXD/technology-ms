package co.com.technology.usecase.synctechnologycapacity;

import co.com.technology.model.technologycapacity.TechnologyCapacity;
import co.com.technology.model.technologycapacity.gateways.TechnologyCapacityRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SyncTechnologyCapacityServiceTest {

    @Mock
    private TechnologyCapacityRepository repository;

    @InjectMocks
    private SyncTechnologyCapacityService service;

    @Test
    void sync_withTechnologyIds_deletesAndSavesNewRecords() {
        Long capacityId = 1L;
        List<Long> technologyIds = List.of(10L, 20L, 30L);

        when(repository.deleteByCapacityExternalId(capacityId)).thenReturn(Mono.empty());
        when(repository.saveAll(org.mockito.ArgumentMatchers.anyIterable())).thenReturn(Flux.empty());

        StepVerifier.create(service.sync(capacityId, technologyIds))
            .verifyComplete();

        verify(repository).deleteByCapacityExternalId(capacityId);
    }

    @Test
    void sync_withEmptyListOnlyDeletesExistingRecords() {
        Long capacityId = 1L;
        List<Long> technologyIds = List.of();

        when(repository.deleteByCapacityExternalId(capacityId)).thenReturn(Mono.empty());

        StepVerifier.create(service.sync(capacityId, technologyIds))
            .verifyComplete();

        verify(repository).deleteByCapacityExternalId(capacityId);
    }
}