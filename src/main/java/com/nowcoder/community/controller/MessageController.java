package com.nowcoder.community.controller;

import com.alibaba.fastjson.JSONObject;
import com.nowcoder.community.entity.Message;
import com.nowcoder.community.entity.Page;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.MessageService;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.util.HtmlUtils;

import java.util.*;

@Controller
public class MessageController implements CommunityConstant {
    @Autowired
    private MessageService messageService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private UserService userService;

    // 私信列表
    @RequestMapping(path = "/letter/list", method = RequestMethod.GET)
    public String getLetter(Model model, Page page) {
        User user = hostHolder.getUser();
        // 分页信息
        page.setLimit(5);;
        page.setPath("/letter/list");
        page.setRows(messageService.findConversationCount(user.getId()));

        // 会话列表
        List<Message> conversations = messageService.findConversations(user.getId(), page.getOffset(), page.getLimit());

        // Vo
        List<Map<String ,Object>> conversationsVo = new ArrayList<>();
        for (Message message : conversations) {
            HashMap<String, Object> map = new HashMap<>();
            map.put("conversation", message);
            map.put("letterCount", messageService.findLetterCount(message.getConversationId()));
            map.put("unreadCount", messageService.findLetterUnreadCount(user.getId(), message.getConversationId()));
            int targetId = user.getId() == message.getFromId() ? message.getToId() : message.getFromId();
            map.put("target", userService.findUserById(targetId));

            conversationsVo.add(map);
        }
        model.addAttribute("conversations", conversationsVo);

        // 查询未读消息数量
        int letterUnreadCount = messageService.findLetterUnreadCount(user.getId(), null);
        model.addAttribute("letterUnreadCount", letterUnreadCount);

        // 查询未读通知数量
        // 查询通知未读消息数量
        int noticeUnreadCount = messageService.findNoticeUnreadCount(user.getId(), null);
        model.addAttribute("noticeUnreadCount", noticeUnreadCount);
        return "/site/letter";
    }

    @RequestMapping(path = "/letter/detail/{conversationId}", method = RequestMethod.GET)
    public String getLetterDetail(@PathVariable("conversationId") String conversationId, Page page, Model model) {
        // 分页信息
        page.setLimit(5);
        page.setPath("/letter/detail/" + conversationId);
        page.setRows(messageService.findLetterCount(conversationId));

        List<Message> letterList = messageService.findLetter(conversationId, page.getOffset(), page.getLimit());
        List<Map<String, Object>> lettersVo = new ArrayList<>();
        for (Message letter : letterList) {
            HashMap<String, Object> map = new HashMap<>();
            map.put("letter", letter);
            map.put("fromUser", userService.findUserById(letter.getFromId()));
            lettersVo.add(map);
        }

        model.addAttribute("letters", lettersVo);

        // 私信目标
        model.addAttribute("target", getLetterTarget(conversationId));

        // 将未读设置已读
        List<Integer> ids = getLetterIds(letterList);
        if (!ids.isEmpty()) {
            messageService.readMessage(ids);
        }

        return "/site/letter-detail";
    }

    // 得到集合中未读消息的id列表
    private List<Integer> getLetterIds(List<Message> letterList) {
        ArrayList<Integer> ids = new ArrayList<>();

        for (Message message : letterList) {
             if (hostHolder.getUser().getId() == message.getToId() && message.getStatus() == 0) {
                 ids.add(message.getId());
             }
        }

        return ids;
    }

    private User getLetterTarget(String conversationId) {
        String[] ids = conversationId.split("_");
        int id0 = Integer.parseInt(ids[0]);
        int id1 = Integer.parseInt(ids[1]);

        if (hostHolder.getUser().getId() == id0) {
            return userService.findUserById(id1);
        } else {
            return userService.findUserById(id0);
        }
    }

    // 异步发送私信接口
    @RequestMapping(path = "/letter/send", method = RequestMethod.POST)
    @ResponseBody // 异步
    public String sendLetter(String toName, String content) {
        User user = userService.findUserByName(toName);
        if (user == null) {
            return CommunityUtil.getJsonString(1, "目标用户不存在！");
        }

        Message message = new Message();
        message.setContent(content);
        message.setFromId(hostHolder.getUser().getId());
        message.setCreateTime(new Date());
        message.setToId(user.getId());
        String conversationId;
        if (hostHolder.getUser().getId() < user.getId()) {
            conversationId = hostHolder.getUser().getId() + "_" + user.getId();
        } else {
            conversationId = user.getId() + "_" + hostHolder.getUser().getId();
        }
        message.setConversationId(conversationId);
        messageService.addMessage(message);

        // 报错统一处理异常
        return CommunityUtil.getJsonString(0);
    }

    @RequestMapping(path = "notice/list", method = RequestMethod.GET)
    public String getNoticeList(Model model) {
        User user = hostHolder.getUser();

        // 查询评论类通知
        Message message = messageService.findLatestNotice(user.getId(), TOPIC_COMMENT);
        HashMap<String, Object> messageVo = new HashMap<>();
        if (message != null) {
            messageVo.put("message", message);

            String content = HtmlUtils.htmlUnescape(message.getContent());
            HashMap<String, Object> data = JSONObject.parseObject(content, HashMap.class);

            messageVo.put("user", userService.findUserById((Integer) data.get("userId")));
            messageVo.put("entityType", data.get("entityType"));
            messageVo.put("entityId", data.get("entityId"));
            messageVo.put("postId", data.get("postId"));

            int noticeCount = messageService.findNoticeCount(user.getId(), TOPIC_COMMENT);
            messageVo.put("count", noticeCount);

            int noticeUnreadCount = messageService.findNoticeUnreadCount(user.getId(), TOPIC_COMMENT);
            messageVo.put("unread", noticeUnreadCount);
            model.addAttribute("commentNotice", messageVo);
        }

        // 查看点赞类通知
        message = messageService.findLatestNotice(user.getId(), TOPIC_LIKE);
        messageVo = new HashMap<>();
        if (message != null) {
            messageVo.put("message", message);

            String content = HtmlUtils.htmlUnescape(message.getContent());
            HashMap<String, Object> data = JSONObject.parseObject(content, HashMap.class);

            messageVo.put("user", userService.findUserById((Integer) data.get("userId")));
            messageVo.put("entityType", data.get("entityType"));
            messageVo.put("entityId", data.get("entityId"));
            messageVo.put("postId", data.get("postId"));

            int noticeCount = messageService.findNoticeCount(user.getId(), TOPIC_LIKE);
            messageVo.put("count",noticeCount);

            int noticeUnreadCount = messageService.findNoticeUnreadCount(user.getId(), TOPIC_LIKE);
            messageVo.put("unread", noticeUnreadCount);
            model.addAttribute("likeNotice", messageVo);
        }

        // 查看关注类通知
        message = messageService.findLatestNotice(user.getId(), TOPIC_FOLLOW);
        messageVo = new HashMap<>();
        if (message != null) {
            messageVo.put("message", message);

            String content = HtmlUtils.htmlUnescape(message.getContent());
            HashMap<String, Object> data = JSONObject.parseObject(content, HashMap.class);

            messageVo.put("user", userService.findUserById((Integer) data.get("userId")));
            messageVo.put("entityType", data.get("entityType"));
            messageVo.put("entityId", data.get("entityId"));

            int noticeCount = messageService.findNoticeCount(user.getId(), TOPIC_FOLLOW);
            messageVo.put("count",noticeCount);

            int noticeUnreadCount = messageService.findNoticeUnreadCount(user.getId(), TOPIC_FOLLOW);
            messageVo.put("unread", noticeUnreadCount);
            model.addAttribute("followNotice", messageVo);
        }

        // 查询私信未读消息数量
        int letterUnreadCount = messageService.findLetterUnreadCount(user.getId(), null);
        model.addAttribute("letterUnreadCount", letterUnreadCount);

        // 查询通知未读消息数量
        int noticeUnreadCount = messageService.findNoticeUnreadCount(user.getId(), null);
        model.addAttribute("noticeUnreadCount", noticeUnreadCount);

        return "site/notice";
    }

    @RequestMapping(path = "notice/detail/{topic}", method = RequestMethod.GET)
    public String getNoticeDetail(@PathVariable("topic") String topic, Page page, Model model) {
        User user = hostHolder.getUser();

        page.setLimit(5);
        page.setPath("/notice/detail/" + topic);
        page.setRows(messageService.findNoticeCount(user.getId(), topic));

        List<Message> notices = messageService.findNotices(user.getId(), topic, page.getOffset(), page.getLimit());
        ArrayList<Map<String, Object>> noticesVo = new ArrayList<>();
        for (Message notice : notices) {
            HashMap<String, Object> map = new HashMap<>();
            // 通知
            map.put("notice", notice);
            // 通知内容
            String content = HtmlUtils.htmlUnescape(notice.getContent());
            HashMap data = JSONObject.parseObject(content, HashMap.class);
            map.put("user",userService.findUserById((Integer) data.get("userId")));
            map.put("entityType", data.get("entityType"));
            map.put("entityId", data.get("entityId"));
            map.put("postId", data.get("postId"));
            // 通知作者
            map.put("fromUser", userService.findUserById(notice.getFromId()));

            noticesVo.add(map);
        }
        model.addAttribute("notices", noticesVo);

        // 设置已读
        List<Integer> ids = getLetterIds(notices);
        if (!ids.isEmpty()) {
            messageService.readMessage(ids);
        }

        return "site/notice-detail";
    }
}
