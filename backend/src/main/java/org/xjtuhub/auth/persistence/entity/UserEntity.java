package org.xjtuhub.auth.persistence.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.sql.Timestamp;

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
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }
    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }
    public String getAccountStatus() { return accountStatus; }
    public void setAccountStatus(String accountStatus) { this.accountStatus = accountStatus; }
    public String getAuthLevel() { return authLevel; }
    public void setAuthLevel(String authLevel) { this.authLevel = authLevel; }
    public String getPrimaryIdentityProvider() { return primaryIdentityProvider; }
    public void setPrimaryIdentityProvider(String primaryIdentityProvider) { this.primaryIdentityProvider = primaryIdentityProvider; }
    public String getLastLoginProvider() { return lastLoginProvider; }
    public void setLastLoginProvider(String lastLoginProvider) { this.lastLoginProvider = lastLoginProvider; }
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }
    public Timestamp getDeletedAt() { return deletedAt; }
    public void setDeletedAt(Timestamp deletedAt) { this.deletedAt = deletedAt; }
}
