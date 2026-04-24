package org.xjtuhub.auth.persistence.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;

@Getter
@Setter
@TableName("sessions")
public class SessionEntity {
    @TableId
    private Long id;
    private Long userId;
    private String sessionTokenHash;
    private String status;
    private String loginProvider;
    private String deviceLabel;
    private String ipAddress;
    private String ipHash;
    private String userAgentHash;
    private Timestamp expiresAt;
    private Timestamp lastSeenAt;
    private Timestamp createdAt;
    private Timestamp updatedAt;
}
