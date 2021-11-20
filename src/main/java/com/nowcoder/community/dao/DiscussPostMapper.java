package com.nowcoder.community.dao;

import com.nowcoder.community.entity.DiscussPost;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DiscussPostMapper {

    // 根据userID查询关联帖子，userId为0，则查询所有帖子，用于首页展示
    // offset：起始行号 limit：查询多少条
    List<DiscussPost> selectDiscussPosts(int userId, int offset, int limit);

    // @Param 为参数起别名
    // 如果参数只有一个参数，并且在<if>里使用，参数必须取别名
    // userId为0，则查询所有帖子数目
    int selectDiscussPostRows(@Param("userId") int userId);
}