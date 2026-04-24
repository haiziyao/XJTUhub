package org.xjtuhub.admin.persistence.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;

@Getter
@Setter
@TableName("audit_logs")
public class AuditLogEntity {
    @TableId
    private Long id;
    private Long actorUserId;
    private Long adminAccountId;
    private String action;
    private String targetType;
    private Long targetId;
    private String requestId;
    private String ipAddress;
    private String ipHash;
    private String userAgentHash;
    private String detailsJson;
    private Timestamp createdAt;
}
