package com.nowcoder.community.util;

public class RedisKeyUtil {

    // key多单词分割
    private static final String SPLIT = ":";
    // 帖子、评论、回复的赞
    private static final String PREFIX_ENTITY_LIKE = "like:entity";
    // 用户得到的赞
    private static final String PREFIX_USER_LIKE = "like:user";

    // 某个实体的赞
    // like:entity:entityType:entityId -> set(userId)    用set而不是整数，是为了可以看到赞来自哪个用户，方便后续功能拓展
    public static String getEntityLikeKey(int entityType, int entityId) {
        return PREFIX_ENTITY_LIKE + SPLIT + entityType + SPLIT + entityId;
    }

    // 某个用户收到的赞
    // like:user:userId -> int
    public static String getUserLikeKey(int userId) {
        return PREFIX_USER_LIKE + SPLIT + userId;
    }
}
