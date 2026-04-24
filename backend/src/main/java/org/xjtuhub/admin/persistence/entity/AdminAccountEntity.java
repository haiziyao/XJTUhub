package org.xjtuhub.admin.persistence.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@TableName("admin_accounts")
public class AdminAccountEntity {
    @TableId
    private Long id;
    private Long userId;
    private String adminRole;
    private String status;
}
