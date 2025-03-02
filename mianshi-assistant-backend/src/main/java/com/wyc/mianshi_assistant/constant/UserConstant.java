package com.wyc.mianshi_assistant.constant;

/**
 * 用户常量
 */
public interface UserConstant {

    /**
     * 用户默认头像地址
     */
    String USER_DEFAULT_AVATAR = "https://wyc-mianshi-assistant.oss-cn-shenzhen.aliyuncs.com/user/0oiw8eks-%E7%94%A8%E6%88%B7%E4%B8%AD%E5%BF%83.png";

    /**
     * 用户登录态键
     */
    String USER_LOGIN_STATE = "user_login";

    //  region 权限

    /**
     * 默认角色
     */
    String DEFAULT_ROLE = "user";

    /**
     * 管理员角色
     */
    String ADMIN_ROLE = "admin";

    /**
     * 被封号
     */
    String BAN_ROLE = "ban";

    // endregion
}
