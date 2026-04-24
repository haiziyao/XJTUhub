package org.xjtuhub.auth.persistence.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;

@Getter
@Setter
@TableName("user_auth_identities")
public class UserAuthIdentityEntity {
    @TableId
    private Long id;
    private Long userId;
    private String provider;
    private String providerSubject;
    private String providerDisplay;
    private String verificationStatus;
    private Timestamp verifiedAt;
    private Timestamp lastUsedAt;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    private Timestamp deletedAt;
}
