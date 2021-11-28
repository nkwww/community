package com.nowcoder.community.controller;

import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.entity.Page;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.DiscussPostService;
import com.nowcoder.community.service.LikeService;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class HomeController implements CommunityConstant {

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private UserService userService;

    @Autowired
    private LikeService likeService;

    // 返回index页面
    @RequestMapping(path = "/index", method = RequestMethod.GET)
    public String getIndexPage(Model model, Page page, @RequestParam(name = "orderMode", defaultValue = "0") int orderMode) {
        // 方法调用前,SpringMVC会自动实例化Model和Page,并将Page注入Model.
        // 所以,在thymeleaf中可以直接访问Page对象中的数据.
        page.setRows(discussPostService.findDiscussPostRows(0));
        page.setPath("/index?orderMode=" + orderMode);

        List<DiscussPost> list = discussPostService.findDiscussPosts(0, page.getOffset(), page.getLimit(), orderMode);
        // 存储组装后的DiscussPost (userId 被替换为 用户名)
        List<Map<String, Object>> discussPosts = new ArrayList<>();

        for (DiscussPost post : list) {
            HashMap<String, Object> map = new HashMap<>();
            map.put("post", post);
            // 查询User
            User user = userService.findUserById(post.getUserId());
            map.put("user", user);
            discussPosts.add(map);
            // 查询帖子点赞数
            long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST, post.getId());
            map.put("likeCount", likeCount);
        }

        // 把数据放到Model中
        model.addAttribute("discussPosts", discussPosts);
        model.addAttribute("orderMode",orderMode);
        return "/index";
    }

    // 500错误 跳转页面路由
    @RequestMapping(path = "/error", method = RequestMethod.POST)
    public String getErrorPage() {
        return "/error/500";
    }

    @RequestMapping(path = "/denied", method = {RequestMethod.GET})
    public String getDeniedPage() {
        return "/error/404";
    }
}
