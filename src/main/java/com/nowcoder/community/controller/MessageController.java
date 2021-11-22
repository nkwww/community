package com.nowcoder.community.controller;

import com.nowcoder.community.entity.Message;
import com.nowcoder.community.entity.Page;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.MessageService;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class MessageController {
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

        return "/site/letter-detail";
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
}