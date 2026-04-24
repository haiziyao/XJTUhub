package org.xjtuhub.admin;

import org.springframework.stereotype.Component;
import org.xjtuhub.auth.InMemoryAuthStore;
import org.xjtuhub.common.support.IdGenerator;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
class InMemoryAdminStore implements AdminStore {
    private final IdGenerator idGenerator;
    private final InMemoryAuthStore inMemoryAuthStore;
    private final Map<Long, StoredAdminAccount> adminAccounts = new ConcurrentHashMap<>();
    private final Map<Long, StoredUserRecord> users = new ConcurrentHashMap<>();
    private final Map<Long, StoredAuditLog> auditLogs = new ConcurrentHashMap<>();

    InMemoryAdminStore(IdGenerator idGenerator, InMemoryAuthStore inMemoryAuthStore) {
        this.idGenerator = idGenerator;
        this.inMemoryAuthStore = inMemoryAuthStore;
    }

    @Override
    public Optional<StoredAdminAccount> findActiveAdminByUserId(long userId) {
        return adminAccounts.values().stream()
                .filter(account -> account.userId() == userId)
                .filter(account -> "active".equals(account.status()))
                .findFirst();
    }

    @Override
    public Optional<StoredUserRecord> findUserById(long userId) {
        StoredUserRecord user = users.get(userId);
        if (user != null) {
            return Optional.of(user);
        }
        return inMemoryAuthStore.findUserById(userId)
                .map(storedUser -> new StoredUserRecord(storedUser.id(), storedUser.authLevel()));
    }

    @Override
    public void markCampusVerification(long targetUserId, long actorUserId, Instant now) {
        StoredUserRecord user = users.get(targetUserId);
        if (user != null) {
            users.put(targetUserId, new StoredUserRecord(user.id(), "campus_app_verified"));
        }
        inMemoryAuthStore.markCampusVerification(targetUserId, now);
    }

    @Override
    public List<StoredAuditLog> listAuditLogs(int offset, int limit) {
        return auditLogs.values().stream()
                .sorted(Comparator.comparing(StoredAuditLog::createdAt).reversed()
                        .thenComparing(Comparator.comparing(StoredAuditLog::id).reversed()))
                .skip(offset)
                .limit(limit)
                .toList();
    }

    @Override
    public long countAuditLogs() {
        return auditLogs.size();
    }

    @Override
    public List<StoredAuditLog> listAuditLogsByActionAndTarget(String action, String targetType, long targetId, int offset, int limit) {
        return auditLogs.values().stream()
                .filter(log -> action.equals(log.action()))
                .filter(log -> targetType.equals(log.targetType()))
                .filter(log -> log.targetId() != null && log.targetId() == targetId)
                .sorted(Comparator.comparing(StoredAuditLog::createdAt).reversed()
                        .thenComparing(Comparator.comparing(StoredAuditLog::id).reversed()))
                .skip(offset)
                .limit(limit)
                .toList();
    }

    @Override
    public long countAuditLogsByActionAndTarget(String action, String targetType, long targetId) {
        return auditLogs.values().stream()
                .filter(log -> action.equals(log.action()))
                .filter(log -> targetType.equals(log.targetType()))
                .filter(log -> log.targetId() != null && log.targetId() == targetId)
                .count();
    }

    @Override
    public void writeAuditLog(Long actorUserId, Long adminAccountId, String action, String targetType, Long targetId, String requestId, String ipAddress, String ipHash, String userAgentHash, String detailsJson, Instant now) {
        long id = idGenerator.nextId();
        auditLogs.put(id, new StoredAuditLog(
                id,
                actorUserId,
                adminAccountId,
                action,
                targetType,
                targetId,
                requestId,
                ipHash,
                userAgentHash,
                detailsJson,
                now
        ));
    }

    void grantAdminAccount(long userId, String adminRole) {
        adminAccounts.put(userId, new StoredAdminAccount(idGenerator.nextId(), userId, adminRole, "active"));
    }

    void putUser(long userId, String authLevel) {
        users.put(userId, new StoredUserRecord(userId, authLevel));
    }
}
