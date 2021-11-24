package com.nowcoder.community.service;


import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class FollowService implements CommunityConstant {
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private UserService userService;

    public void follow(int userId, int entityType, int entityId) {
        // redis事务，关注者增加关注记录，被关注者增加粉丝记录
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations redisOperations) throws DataAccessException {
                String followeeKey = RedisKeyUtil.getFolloweeKey(userId, entityType);
                String followerKey = RedisKeyUtil.getFollowerKey(entityType, entityId);

                // 事务开始
                redisOperations.multi();
                redisOperations.opsForZSet().add(followeeKey, entityId, System.currentTimeMillis());
                redisOperations.opsForZSet().add(followerKey, userId, System.currentTimeMillis());
                return redisOperations.exec();
            }
        });
    }

    public void unfollow(int userId, int entityType, int entityId) {
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations redisOperations) throws DataAccessException {
                String followeeKey = RedisKeyUtil.getFolloweeKey(userId, entityType);
                String followerKey = RedisKeyUtil.getFollowerKey(entityType, entityId);

                // 事务开始
                redisOperations.multi();
                redisOperations.opsForZSet().remove(followeeKey, entityId);
                redisOperations.opsForZSet().remove(followerKey, userId);
                return redisOperations.exec();
            }
        });
    }

    // 查询关注的实体的数据
    public long findFolloweeCount(int userId, int entityType) {
        String followeeKey = RedisKeyUtil.getFolloweeKey(userId, entityType);
        return redisTemplate.opsForZSet().zCard(followeeKey);
    }

    // 查询实体的粉丝数量
    public long findFollowerCount(int entityType, int entityId) {
        String followerKey = RedisKeyUtil.getFollowerKey(entityType, entityId);
        return redisTemplate.opsForZSet().zCard(followerKey);
    }

    // 查询当前用户是否关注该实体
    public boolean hasFollowed(int userId, int entityType, int entityId) {
        String followeeKey = RedisKeyUtil.getFolloweeKey(userId, entityType);
        return redisTemplate.opsForZSet().score(followeeKey, entityId) != null;
    }

    // 查询某用户关注的人
    public List<Map<String, Object>> findFollowee(int userId, int offset, int limit) {
        String followeeKey = RedisKeyUtil.getFolloweeKey(userId, ENTITY_TYPE_USER);
        Set<Object> targetIds = redisTemplate.opsForZSet().reverseRange(followeeKey, offset, offset + limit - 1);

        if (targetIds == null) {
            return null;
        }

        List<Map<String, Object>> list = new ArrayList<>();
        for (Object targetId : targetIds) {
            Integer target = (Integer) targetId;
            HashMap<String, Object> map = new HashMap<>();
            map.put("user", userService.findUserById(target));
            Double score = redisTemplate.opsForZSet().score(followeeKey, target);
            map.put("followTime", new Date(score.longValue()));
            list.add(map);
        }

        return list;
    }

    // 查询某用户的粉丝
    public List<Map<String, Object>> findFollower(int userId, int offset, int limit) {
        String followerKey = RedisKeyUtil.getFollowerKey(ENTITY_TYPE_USER, userId);
        Set<Object> followers = redisTemplate.opsForZSet().reverseRange(followerKey, offset, offset + limit - 1);

        if (followers == null) {
            return null;
        }

        List<Map<String, Object>> list = new ArrayList<>();
        for (Object f : followers) {
            Integer follower = (Integer) f;
            HashMap<String, Object> map = new HashMap<>();
            map.put("user", userId);
            Double score = redisTemplate.opsForZSet().score(followerKey, follower);
            map.put("followTime", new Date(score.longValue()));
        }
        return list;
    }
}
