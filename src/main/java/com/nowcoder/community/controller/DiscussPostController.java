package com.nowcoder.community.controller;

import com.nowcoder.community.entity.Comment;
import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.entity.Page;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.CommentService;
import com.nowcoder.community.service.DiscussPostService;
import com.nowcoder.community.service.LikeService;
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

import java.util.*;

@Controller
@RequestMapping("/discuss")
public class DiscussPostController implements CommunityConstant {

    @Autowired
    private CommentService commentService;

    @Autowired
    private UserService userService;

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private LikeService likeService;

    @RequestMapping(path = "/add", method = RequestMethod.POST)
    @ResponseBody
    public String addDiscussPost(String title, String content) {
        User user = hostHolder.getUser();
        if (user == null) {
            return CommunityUtil.getJsonString(403, "你还没有登录！");
        }

        DiscussPost discussPost = new DiscussPost();
        discussPost.setUserId(user.getId());
        discussPost.setTitle(title);
        discussPost.setContent(content);
        discussPost.setCreateTime(new Date());
        discussPostService.addDiscussPost(discussPost);

        // service层如果报错就统一处理
        return CommunityUtil.getJsonString(0, "发布成功");
    }

    @RequestMapping(value = "/detail/{discussPostId}", method = RequestMethod.GET)
    public String getDiscussPost(@PathVariable("discussPostId") int discussPostId, Model model, Page page) {
        // 帖子
        DiscussPost discussPost = discussPostService.findDiscussPostById(discussPostId);
        model.addAttribute("post", discussPost);
        // 处理帖子里面的用户id，再次查询，后面可以缓存到redis中而不是查库
        User user = userService.findUserById(discussPost.getUserId());
        model.addAttribute("user", user);
        // 帖子点赞数量
        // 查询帖子点赞数
        long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST, discussPostId);
        model.addAttribute("likeCount", likeCount);
        // 查询当前用户对该帖子的点赞状态

        // 当前用户未登录则点赞状态为0，否则从redis中查询
        int likeStatus = (hostHolder.getUser() == null) ? 0 :
                likeService.findEntityLikeStatus(ENTITY_TYPE_POST, discussPostId, hostHolder.getUser().getId());
        model.addAttribute("likeStatus", likeStatus);

        // 帖子回复
        page.setLimit(5);;
        page.setPath("/discuss/detail/" + discussPostId);
        page.setRows(discussPost.getCommentCount());

        // 评论： 给帖子的评论
        // 回复： 给评论的评论

        // 评论列表
        List<Comment> commentList = commentService.
                findCommentByEntity(ENTITY_TYPE_POST, discussPost.getId(), page.getOffset(), page.getLimit());

        // userId转user对象
        // 评论VO列表
        List<Map<String, Object>> commentVoList = new ArrayList<>();
        for (Comment comment : commentList) {
            // 一个评论的VO
            Map<String, Object> commentVo = new HashMap<>();
            commentVo.put("comment", comment);
            commentVo.put("user", userService.findUserById(comment.getUserId()));
            // 查询评论点赞数
            likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_COMMENT, comment.getEntityId());
            commentVo.put("likeCount", likeCount);
            // 查询当前用户对该评论的点赞状态
            // 当前用户未登录则点赞状态为0，否则从redis中查询
            likeStatus = (hostHolder.getUser() == null) ? 0 :
                    likeService.findEntityLikeStatus(ENTITY_TYPE_COMMENT, discussPostId, hostHolder.getUser().getId());
            commentVo.put("likeStatus", likeStatus);

            // 查询回复
            List<Comment> replyList = commentService.findCommentByEntity(ENTITY_TYPE_COMMENT, comment.getId(), 0, Integer.MAX_VALUE);

            // 回复VO列表
            List<Map<String, Object>> replyVoList = new ArrayList<>();
            for (Comment reply : replyList) {
                Map<String, Object> replyVo = new HashMap<>();
                // 回复内容
                replyVo.put("reply", reply);
                // 回复者
                replyVo.put("user", userService.findUserById(reply.getUserId()));
                // 回复目标
                User target = reply.getTargetId() == 0 ? null : userService.findUserById(reply.getTargetId());
                replyVo.put("target", target);
                // 回复点赞信息
                // 查询回复点赞数
                likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_COMMENT, reply.getEntityId());
                replyVo.put("likeCount", likeCount);
                // 查询当前用户对该回复的点赞状态
                // 当前用户未登录则点赞状态为0，否则从redis中查询
                likeStatus = (hostHolder.getUser() == null) ? 0 :
                        likeService.findEntityLikeStatus(ENTITY_TYPE_COMMENT, reply.getEntityId(), hostHolder.getUser().getId());
                replyVo.put("likeStatus", likeStatus);
                replyVoList.add(replyVo);
            }
            commentVo.put("replys", replyVoList);
            // 回复数量
            int replyCount = commentService.findCommentCount(ENTITY_TYPE_COMMENT, comment.getId());
            commentVo.put("replyCount", replyCount);
            commentVoList.add(commentVo);
        }

        model.addAttribute("comments", commentVoList);
        return "/site/discuss-detail";
    }
}
