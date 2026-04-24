package org.xjtuhub.auth.persistence.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;

@Getter
@Setter
@TableName("email_verification_tokens")
public class EmailVerificationTokenEntity {
    @TableId
    private Long id;
    private String email;
    private String tokenHash;
    private String purpose;
    private String status;
    private Timestamp expiresAt;
    private Timestamp consumedAt;
    private Timestamp createdAt;
}
