package com.nowcoder.community.dao;

import com.nowcoder.community.entity.Message;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface MessageMapper {

    // 查询当前用户的会话列表，针对每个会话只返回最新私信
    List<Message> selectConversations(int userId, int offset, int limit);

    // 查询当前用户的会话数量
    int selectConversationCount (int userId);

    // 查询某个会话所包含的私信列表
    List<Message> selectLetter(String conversationId, int offset, int limit);

    // 查询某个会话所包含的私信数量
    int selectLetterCount(String conversationId);

    // 查询未读私信的数量,包括总的未读私信，和某个会话的未读私信
    int selectLetterUnreadCount(int userId, String conversationId);

    // 新增私信
    int insertMessage(Message message);

    // 更新私信状态
    int updateStatus(List<Integer> ids, int status);

    // 查某个主题下的最新的通知
    Message selectLatestNotice(int userId, String topic);

    // 某个主题所包含的通知数量
    int selectNoticeCount(int userId, String topic);

    // 查询未读的通知的数量
    // topic为null则查所有主题的数量
    int selectNoticeUnreadCount(int userId, String topic);

    // 查询某个主题包含的所有通知
    List<Message> selectNotices(int userId, String topic, int offset, int limit);
}
