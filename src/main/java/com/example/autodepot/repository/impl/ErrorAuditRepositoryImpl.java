package com.example.autodepot.repository.impl;

import com.example.autodepot.entity.ErrorAudit;
import com.example.autodepot.repository.ErrorAuditRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Repository
public class ErrorAuditRepositoryImpl implements ErrorAuditRepository {

    private final JdbcTemplate jdbcTemplate;

    public ErrorAuditRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public ErrorAudit save(ErrorAudit audit) {
        var keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(con -> {
            var ps = con.prepareStatement(
                "INSERT INTO error_audit (created_at, thread_name, location, exception_type, message) VALUES (?, ?, ?, ?, ?)",
                java.sql.Statement.RETURN_GENERATED_KEYS);
            ps.setTimestamp(1, Timestamp.from(audit.getCreatedAt()));
            ps.setString(2, audit.getThreadName());
            ps.setString(3, audit.getLocation());
            ps.setString(4, audit.getExceptionType());
            ps.setString(5, audit.getMessage());
            return ps;
        }, keyHolder);
        Number id = (Number) keyHolder.getKeys().get("id");
        audit.setId(id != null ? id.longValue() : 0L);
        return audit;
    }

    @Override
    public Page<ErrorAudit> findAllFiltered(String exceptionType, Instant since, Pageable pageable) {
        var filterParams = new ArrayList<Object>();
        var where = new StringBuilder();
        if (exceptionType != null) {
            where.append(" AND exception_type = ?");
            filterParams.add(exceptionType);
        }
        if (since != null) {
            where.append(" AND created_at >= ?");
            filterParams.add(Timestamp.from(since));
        }
        String whereClause = where.length() > 0 ? " WHERE " + where.substring(5) : "";

        Long total = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM error_audit" + whereClause,
            Long.class, filterParams.toArray());
        if (total == null) total = 0L;

        var queryParams = new ArrayList<Object>(filterParams);
        queryParams.add(pageable.getPageSize());
        queryParams.add(pageable.getOffset());
        String sql = "SELECT id, created_at, thread_name, location, exception_type, message FROM error_audit"
            + whereClause + " ORDER BY created_at DESC LIMIT ? OFFSET ?";

        List<ErrorAudit> content = jdbcTemplate.query(sql, (rs, rowNum) -> mapErrorAudit(rs), queryParams.toArray());
        return new PageImpl<>(content, pageable, total);
    }

    @Override
    public void deleteAll() {
        jdbcTemplate.update("DELETE FROM error_audit");
    }

    private static ErrorAudit mapErrorAudit(java.sql.ResultSet rs) throws java.sql.SQLException {
        var e = new ErrorAudit();
        e.setId(rs.getLong("id"));
        Timestamp ts = rs.getTimestamp("created_at");
        e.setCreatedAt(ts != null ? ts.toInstant() : null);
        e.setThreadName(rs.getString("thread_name"));
        e.setLocation(rs.getString("location"));
        e.setExceptionType(rs.getString("exception_type"));
        e.setMessage(rs.getString("message"));
        return e;
    }
}
