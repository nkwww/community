package com.nowcoder.community.dao.elasticsearch;

import com.nowcoder.community.entity.DiscussPost;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

// spring提供针对 数据访问的 接口
@Repository                                                      // <处理的实体类，实体类的主键>
public interface DiscussPostRepository extends ElasticsearchRepository<DiscussPost, Integer> {  // ElasticsearchRepository 实现了对es增删改查的方法

}
