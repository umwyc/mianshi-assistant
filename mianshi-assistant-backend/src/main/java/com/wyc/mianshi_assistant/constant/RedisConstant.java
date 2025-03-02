package com.wyc.mianshi_assistant.constant;

/**
 * Redis 常量
 */
public interface RedisConstant {

    /**
     * 用户签到记录的 Redis Key 前缀
     */
    String USER_SIGN_IN_REDIS_KEY_PREFIX = "user:signings:";

    /**
     * 获取用户签到记录的 Redis Key
     *
     * @param year
     * @param userId
     * @return
     */
    static String getUserSignInRedisKey(int year, Long userId) {
        return String.format("%s:%s:%s", USER_SIGN_IN_REDIS_KEY_PREFIX, year, userId);
    }

}
