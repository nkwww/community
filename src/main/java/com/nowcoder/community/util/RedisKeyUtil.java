package com.nowcoder.community.util;

public class RedisKeyUtil {

    // key多单词分割
    private static final String SPLIT = ":";
    // 帖子、评论、回复的赞
    private static final String PREFIX_ENTITY_LIKE = "like:entity";
    // 用户得到的赞
    private static final String PREFIX_USER_LIKE = "like:user";
    // 关注目标
    private static final String PREFIX_FOLLOWEE = "followee";
    // 粉丝
    private static final String PREFIX_FOLLOWER = "follower";
    // 验证码
    private static final String PREFIX_KAPTCHA = "kaptcha";
    // 登录凭证
    private static final String PREFIX_TICKET = "ticket";
    // 缓存用户
    private static final String PREFIX_USER = "user";
    // 根据ip 排重计算日访问
    private static final String PREFIX_UV = "uv";
    // 根据用户id,排重计算日活跃
    private static final String PREFIX_DAU = "dau";
    // 统计帖子分数的key
    private static final String PREFIX_POST = "post";

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

    // 某个用户的关注实体
    // followee:userId:entityType -> zset(entityId , now)
    public static String getFolloweeKey(int userId, int entityType) {
        return PREFIX_FOLLOWEE + SPLIT + userId + SPLIT + entityType;
    }

    // 某个实体拥有的粉丝
    // follower:entityType:entityId: -> zset(userId , now)
    public static String getFollowerKey(int entityType, int entityId) {
        return PREFIX_FOLLOWER + SPLIT + entityType + SPLIT + entityId;
    }

    // 验证码
    // 用户尚未登录，先随机生成一个字符串作为key，将这个字符串作为cookie传给浏览器
    public static String getKaptchaKey(String owner) {
        return PREFIX_KAPTCHA + SPLIT + owner;
    }

    // 登录凭证
    public static String getTicketKey(String ticket) {
        return PREFIX_TICKET + SPLIT + ticket;
    }

    // 用户
    public static String getUserKey(int userId) {
        return PREFIX_USER + SPLIT + userId;
    }

    // 单日uv
    public static String getUVKey(String date) {
        return PREFIX_UV + SPLIT + date;
    }

    // 区间uv
    public static String getUVKey(String startDate, String endDate) {
        return PREFIX_UV + SPLIT + startDate + SPLIT + endDate;
    }

    // 单日 DAU
    public static String getDAUKey(String date) {
        return PREFIX_DAU + SPLIT + date;
    }

    // 区间  DAU
    public static String getDAUKey(String startDate, String endDate) {
        return PREFIX_DAU + SPLIT + startDate + SPLIT + endDate;
    }

    public static String getPostScoreKey() {
        return PREFIX_POST + SPLIT + "score";
    }
}
