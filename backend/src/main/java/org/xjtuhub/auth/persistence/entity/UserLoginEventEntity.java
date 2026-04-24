package org.xjtuhub.auth.persistence.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;

@Getter
@Setter
@TableName("user_login_events")
public class UserLoginEventEntity {
    @TableId
    private Long id;
    private Long userId;
    private String provider;
    private String eventType;
    private boolean success;
    private String failureReason;
    private String ipAddress;
    private String ipHash;
    private String userAgentHash;
    private Timestamp createdAt;
}
