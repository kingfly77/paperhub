package com.fly.paperhub.user.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("user")
public class UserEntity {

    // default profile url
    public static final String DFT_PROFILE = "https://paperhub.oss-cn-chengdu.aliyuncs.com/user_profiles/paperhub_default_profile.jpeg";

    @TableId
    private Long uid;

    private String username;

    private String password;

    // profile url
    private String profile;

    @TableLogic(value = "1", delval = "0")
    private Integer exist;

    public String getProfile() {
        return this.profile == null || this.profile.isEmpty() ? DFT_PROFILE : this.profile;
    }

}
