package com.nowcoder.community.service;

import com.nowcoder.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class LikeService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     *  点赞
     * @param userId    谁点赞
     * @param entityType    被点赞的实体类型
     * @param entityId      被点赞的实体id
     */
    public void like(int userId, int entityType, int entityId) {
        String key = RedisKeyUtil.getEntityLikeKey(entityType, entityId);

        // 如果集合中存在 点赞用户的id ，本次操作将被翻译为取消点赞
        boolean isUnlike = redisTemplate.opsForSet().isMember(key, userId);

        if (isUnlike) {
            redisTemplate.opsForSet().remove(key, userId);
        } else {
            redisTemplate.opsForSet().add(key, userId);
        }
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
