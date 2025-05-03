package org.example.prodcatservice.repositories;

import org.example.prodcatservice.models.FailedIndexTask;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FailedIndexTaskRepository extends JpaRepository<FailedIndexTask, Long> {
    List<FailedIndexTask> findTop10ByOrderByRetryCountAscLastTriedAtAsc();
}
