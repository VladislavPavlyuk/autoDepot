package com.example.autodepot.repository;

import com.example.autodepot.entity.ErrorAudit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;

@Repository
public interface ErrorAuditRepository extends JpaRepository<ErrorAudit, Long> {

    @Query("SELECT e FROM ErrorAudit e WHERE (:exceptionType IS NULL OR e.exceptionType = :exceptionType) AND (:since IS NULL OR e.createdAt >= :since) ORDER BY e.createdAt DESC")
    Page<ErrorAudit> findAllFiltered(@Param("exceptionType") String exceptionType, @Param("since") Instant since, Pageable pageable);
}
