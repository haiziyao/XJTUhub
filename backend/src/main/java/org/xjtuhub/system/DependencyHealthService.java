package org.xjtuhub.system;

import io.minio.BucketExistsArgs;
import io.minio.MinioClient;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class DependencyHealthService {
    private final ObjectProvider<JdbcTemplate> jdbcTemplate;
    private final ObjectProvider<StringRedisTemplate> redisTemplate;
    private final ObjectProvider<MinioClient> minioClient;
    private final MinioProperties minioProperties;

    public DependencyHealthService(
            ObjectProvider<JdbcTemplate> jdbcTemplate,
            ObjectProvider<StringRedisTemplate> redisTemplate,
            ObjectProvider<MinioClient> minioClient,
            MinioProperties minioProperties
    ) {
        this.jdbcTemplate = jdbcTemplate;
        this.redisTemplate = redisTemplate;
        this.minioClient = minioClient;
        this.minioProperties = minioProperties;
    }

    public Map<String, DependencyHealthStatus> check() {
        Map<String, DependencyHealthStatus> result = new LinkedHashMap<>();
        result.put("mysql", checkMysql());
        result.put("redis", checkRedis());
        result.put("minio", checkMinio());
        return result;
    }

    private DependencyHealthStatus checkMysql() {
        JdbcTemplate client = jdbcTemplate.getIfAvailable();
        if (client == null) {
            return DependencyHealthStatus.skipped("JdbcTemplate is not configured.");
        }

        try {
            client.queryForObject("SELECT 1", Integer.class);
            return DependencyHealthStatus.ok("MySQL responded to SELECT 1.");
        } catch (RuntimeException ex) {
            return DependencyHealthStatus.down(ex.getClass().getSimpleName());
        }
    }

    private DependencyHealthStatus checkRedis() {
        StringRedisTemplate client = redisTemplate.getIfAvailable();
        if (client == null || client.getConnectionFactory() == null) {
            return DependencyHealthStatus.skipped("StringRedisTemplate is not configured.");
        }

        try (RedisConnection connection = client.getConnectionFactory().getConnection()) {
            String pong = connection.ping();
            if ("PONG".equalsIgnoreCase(pong)) {
                return DependencyHealthStatus.ok("Redis responded to PING.");
            }
            return DependencyHealthStatus.down("Unexpected Redis PING response.");
        } catch (RuntimeException ex) {
            return DependencyHealthStatus.down(ex.getClass().getSimpleName());
        }
    }

    private DependencyHealthStatus checkMinio() {
        MinioClient client = minioClient.getIfAvailable();
        if (client == null) {
            return DependencyHealthStatus.skipped("MinIO client is not configured.");
        }

        try {
            boolean exists = client.bucketExists(BucketExistsArgs.builder()
                    .bucket(minioProperties.bucketName())
                    .build());
            if (exists) {
                return DependencyHealthStatus.ok("MinIO bucket exists.");
            }
            return DependencyHealthStatus.down("Configured MinIO bucket does not exist.");
        } catch (Exception ex) {
            return DependencyHealthStatus.down(ex.getClass().getSimpleName());
        }
    }
}
