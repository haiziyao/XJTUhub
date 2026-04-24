package org.xjtuhub.auth.persistence.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;

@Getter
@Setter
@TableName("users")
public class UserEntity {
    @TableId
    private Long id;
    private String nickname;
    private String avatarUrl;
    private String bio;
    private String accountStatus;
    private String authLevel;
    private String primaryIdentityProvider;
    private String lastLoginProvider;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    private Timestamp deletedAt;
}
