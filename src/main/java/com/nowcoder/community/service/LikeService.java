package com.nowcoder.community.service;

import com.nowcoder.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

@Service
public class LikeService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 点赞
     *
     * @param userId       谁点赞
     * @param entityType   被点赞的实体类型
     * @param entityId     被点赞的实体id
     * @param entityUserId 被点赞的实体用户id
     */
    // 对用户的赞和对实体的赞需要用事务
    public void like(int userId, int entityType, int entityId, int entityUserId) {
//        String key = RedisKeyUtil.getEntityLikeKey(entityType, entityId);
//
        // 如果集合中存在 点赞用户的id ，本次操作将被翻译为取消点赞
//        boolean isUnlike = redisTemplate.opsForSet().isMember(key, userId);
//
//        if (isUnlike) {
//            redisTemplate.opsForSet().remove(key, userId);
//        } else {
//            redisTemplate.opsForSet().add(key, userId);
//        }
        // 添加对用户的赞的逻辑
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations redisOperations) throws DataAccessException {
                String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType, entityId);
                String userLikeKey = RedisKeyUtil.getUserLikeKey(entityUserId);
                // 如果集合中存在 点赞用户的id ，本次操作将被翻译为取消点赞
                boolean isUnlike = redisTemplate.opsForSet().isMember(entityLikeKey, userId);

                // 开启事务
                redisOperations.multi();
                if (isUnlike) {
                    redisTemplate.opsForSet().remove(entityLikeKey, userId);
                    redisTemplate.opsForValue().decrement(userLikeKey);
                } else {
                    redisTemplate.opsForSet().add(entityLikeKey, userId);
                    redisTemplate.opsForValue().increment(userLikeKey);
                }
                return redisOperations.exec();
            }
        });
    }

    // 查询某个用户获得的赞
    public int findUserLikeCount(int userId) {
        String userLikeKey = RedisKeyUtil.getUserLikeKey(userId);
        Integer count = (Integer) redisTemplate.opsForValue().get(userLikeKey);
        return (count == null) ? 0 : count;
    }

    // 查询某个实体点赞的数量
    public long findEntityLikeCount(int entityType, int entityId) {
        String key = RedisKeyUtil.getEntityLikeKey(entityType, entityId);
        return redisTemplate.opsForSet().size(key);
    }

    // 查询某人对某个实体的点赞状态 （未操作、点赞、点踩）
    public int findEntityLikeStatus(int entityType, int entityId, int userId) {
        String key = RedisKeyUtil.getEntityLikeKey(entityType, entityId);
        return redisTemplate.opsForSet().isMember(key, userId) ? 1 : 0;
    }
}
