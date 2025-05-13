package org.example.prodcatservice.repositories;

import org.example.prodcatservice.models.InventoryAuditLog;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
class InventoryAuditLogRepositoryTest {

    @Autowired
    private InventoryAuditLogRepository repository;

    @Test
    void testSearchLogs_byProductId() {
        InventoryAuditLog log = new InventoryAuditLog();
        log.setProductId(123L);
        log.setUpdatedBy("test");
        log.setTimestamp(Instant.now());
        repository.save(log);

        var result = repository.searchLogs(123L, null, null, null, org.springframework.data.domain.PageRequest.of(0, 10));
        assertEquals(1, result.getTotalElements());
    }
}