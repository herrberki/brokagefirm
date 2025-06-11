package com.myproject.brokagefirmchallenge.repo.repository;

import com.myproject.brokagefirmchallenge.repo.entity.AuditLog;
import org.springframework.stereotype.Repository;

@Repository
public interface AuditLogRepository extends BaseRepository<AuditLog, Long> {
}
