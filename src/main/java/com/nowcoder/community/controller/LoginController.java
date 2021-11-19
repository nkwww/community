package com.nowcoder.community.controller;

import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Map;

@Controller
public class LoginController implements CommunityConstant {
    @Autowired
    private UserService userService;

    @RequestMapping(path = "/register", method = RequestMethod.GET)
    public String getRegisterPage() {
        return "/site/register";
    }

    @RequestMapping(path = "/login", method = RequestMethod.GET)
    public String getLoginPage() {
        return "/site/login";
    }

    @RequestMapping(path = "/register", method = RequestMethod.POST)
    public String register(Model model, User user) {
        Map<String, Object> result = userService.register(user);
        if (result == null || result.isEmpty()) {
            // 注册成功，使用中间页跳转到首页
            model.addAttribute("msg", "注册成功，我们已经向您的邮箱发送了一封激活邮件，请尽快激活！");
            // 跳转target
            model.addAttribute("target", "index");
            return "/site/operate-result";
        } else {
            // 注册失败
            model.addAttribute("usernameMsg", result.get("usernameMsg"));
            model.addAttribute("passwordMsg", result.get("passwordMsg"));
            model.addAttribute("emailMsg", result.get("emailMsg"));
            return "/site/register";
        }
    }

    @RequestMapping(path = "/activation/{userId}/{code}", method = RequestMethod.GET)
    public String activation(Model model, @PathVariable("userId") int userId, @PathVariable("code") String code) {
        int activation = userService.activation(userId, code);
        if (activation == ACTIVATION_SUCCESS) {
            // 成功了跳转中间页面，提示成功
            model.addAttribute("msg", "激活成功，您的账号已经可以正常使用了！");
            model.addAttribute("target", "/login");
        } else if (activation == ACTIVATION_REPEAT) {
            // 失败了跳转中间页面，提示失败
            model.addAttribute("msg", "无效操作，该账号已经激活过了！");
            model.addAttribute("target", "/index");
        } else {
            // 失败了跳转中间页面，提示失败
            model.addAttribute("msg", "激活失败，激活码不正确！");
            model.addAttribute("target", "/index");
        }
        return "/site/operate-result";
    }
}
