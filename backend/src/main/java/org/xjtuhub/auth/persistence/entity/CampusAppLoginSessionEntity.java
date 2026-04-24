package org.xjtuhub.auth.persistence.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;

@Getter
@Setter
@TableName("campus_app_login_sessions")
public class CampusAppLoginSessionEntity {
    @TableId
    private Long id;
    private String sceneId;
    private String qrTokenHash;
    private String status;
    private Long matchedUserId;
    private Timestamp expiresAt;
    private Timestamp scannedAt;
    private Timestamp confirmedAt;
    private Timestamp canceledAt;
    private Timestamp createdAt;
    private Timestamp updatedAt;
}
