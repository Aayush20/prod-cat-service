package org.example.prodcatservice.repositories;

import org.example.prodcatservice.models.RetryDeadLetterLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RetryDeadLetterLogRepository extends JpaRepository<RetryDeadLetterLog, Long> {
}
