package org.xjtuhub.auth.persistence.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;

@Getter
@Setter
@TableName("user_memberships")
public class UserMembershipEntity {
    @TableId
    private Long id;
    private Long userId;
    private String membershipType;
    private String status;
    private Timestamp startedAt;
    private Timestamp expiresAt;
    private String source;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    private Timestamp deletedAt;
}
