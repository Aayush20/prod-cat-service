package org.example.prodcatservice.repositories;

import org.example.prodcatservice.models.InventoryAuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;

public interface InventoryAuditLogRepository extends JpaRepository<InventoryAuditLog, Long> {

    @Query("SELECT l FROM InventoryAuditLog l WHERE " +
            "(:productId IS NULL OR l.productId = :productId) AND " +
            "(:updatedBy IS NULL OR l.updatedBy = :updatedBy) AND " +
            "(:startDate IS NULL OR l.timestamp >= :startDate) AND " +
            "(:endDate IS NULL OR l.timestamp <= :endDate)")
    Page<InventoryAuditLog> searchLogs(@Param("productId") Long productId,
                                       @Param("updatedBy") String updatedBy,
                                       @Param("startDate") Instant startDate,
                                       @Param("endDate") Instant endDate,
                                       Pageable pageable);
}
