package com.nowcoder.community.controller;

import com.nowcoder.community.annotation.LoginRequired;
import com.nowcoder.community.entity.Page;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.FollowService;
import com.nowcoder.community.service.LikeService;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.HostHolder;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.exception.CosClientException;
import com.qcloud.cos.exception.CosServiceException;
import com.qcloud.cos.model.ObjectMetadata;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.region.Region;
import com.tencent.cloud.CosStsClient;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Controller
@RequestMapping("/user")
public class UserController implements CommunityConstant {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Value("${community.path.upload}")
    private String uploadPath;

    @Value("${community.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Autowired
    private UserService userService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private LikeService likeService;

    @Autowired
    private FollowService followService;

    @Value("${tencent.secretId}")
    private String secretId;

    @Value("${tencent.secretKey}")
    private String secretKey;

    @Value("${tencent.bucket}")
    private String bucket;

    @Value("${tencent.region}")
    private String region;

    @Value("${tencent.baseUrl}")
    private String baseUrl;

    @LoginRequired
    @RequestMapping(path = "/setting", method = RequestMethod.GET)
    public String getSettingPage() {
        return "/site/setting";
    }

    // 重构上传头像接口
    @RequestMapping(path = "/upload", method = RequestMethod.POST)
    public String uploadHeader(MultipartFile headerImage, Model model) {
        if(headerImage == null){
            model.addAttribute("error", "您还没有选择图片!");
            return "/site/setting";
        }
//        // 文件不存在
//        if (headerImage == null) {
//            model.addAttribute("error", "您还没有选择图片!");
//            return "/site/setting";
//        }
//
//        // 重写格式化上传文件的文件名，避免重复
//        String originalFilename = headerImage.getOriginalFilename();
//        String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
//        // 文件没有后缀
//        if (StringUtils.isBlank(suffix)) {
//            model.addAttribute("error", "文件格式不正确!");
//            return "/site/setting";
//        }
//
//        // 生成随机文件名
//        String fileName = CommunityUtil.generateUUID() + suffix;
//        // 确定文件存放路径
//        File dest = new File(uploadPath + "/" + fileName);
//        try {
//            // 存储文件
//            headerImage.transferTo(dest);
//        } catch (IOException e) {
//            logger.error("文件上传失败" + e.getMessage());
//            // 将异常抛出
//            throw new RuntimeException("上传文件失败，服务器发生异常!", e);
//        }
//
//        // 更新当前用户的头像的路径（web访问路径）
//        // http://localhost:80/community/user/header/xx.png
//        User user = hostHolder.getUser();
//        String headerUrl = domain + contextPath + "/user/header/" + fileName;
//        userService.updateHeader(user.getId(), headerUrl);

        // 1 初始化用户身份信息(secretId, secretKey)
        COSCredentials cred = new BasicCOSCredentials(secretId, secretKey);
        // 2 设置bucket的区域, COS地域的简称请参照 https://cloud.tencent.com/document/product/436/6224
        ClientConfig clientConfig = new ClientConfig(new Region(region));
        // 3 生成cos客户端
        COSClient cosclient = new COSClient(cred, clientConfig);
        // 重写格式化上传文件的文件名，避免重复
        String originalFilename = headerImage.getOriginalFilename();
        String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
        // 文件没有后缀
        if (StringUtils.isBlank(suffix)) {
            model.addAttribute("error", "文件格式不正确!");
            return "/site/setting";
        }
        String fileName = CommunityUtil.generateUUID() + suffix;
        String dirName = "/header/";
        // 简单文件上传, 最大支持 5 GB, 适用于小文件上传, 建议 20 M 以下的文件使用该接口
        // 大文件上传请参照 API 文档高级 API 上传
        File localFile = null;
        try {
            localFile = File.createTempFile("temp",null);
            headerImage.transferTo(localFile);
            PutObjectRequest putObjectRequest = new PutObjectRequest(bucket, dirName + fileName, localFile);
            cosclient.putObject(putObjectRequest);
            //tencent.baseUrl= https://community-1301810288.cos.ap-shanghai.myqcloud.com
            logger.info("上传成功： " + baseUrl + dirName + fileName);
        } catch (IOException e) {
            logger.error("上传失败： " + e.getMessage());
        }finally {
            // 关闭客户端(关闭后台线程)
            cosclient.shutdown();
        }
        // 更新当前用户的头像的路径（web访问路径）
//        https://community-1301810288.cos.ap-shanghai.myqcloud.com/59cdef3585ce4907b2d6fa31847ceb61.jpg
        User user = hostHolder.getUser();
        String headerUrl = baseUrl + dirName + fileName;
        userService.updateHeader(user.getId(), headerUrl);
        return "redirect:/index";
    }

    // 废弃
    @Deprecated
    @RequestMapping(path = "/header/{fileName}", method = RequestMethod.GET)
    public void getHeader(@PathVariable("fileName") String fileName, HttpServletResponse response) {
        // 使用IO流返回图像给浏览器
        // 服务器存放路径
        fileName = uploadPath + "/" + fileName;
        // 文件后缀
        String suffix = fileName.substring(fileName.lastIndexOf("."));
        // 响应图片
        response.setContentType("image/" + suffix);
        try (
                FileInputStream fileInputStream = new FileInputStream(fileName);
                OutputStream outputStream = response.getOutputStream();
        ) {
            byte[] buffer = new byte[1024];
            int b = 0;
            while ((b = fileInputStream.read(buffer)) != -1) {
                outputStream.write(buffer,0,b);
            }
        } catch (IOException e) {
            logger.error("读取头像失败!" + e.getMessage());
        }
    }

    // 修改密码接口
    @LoginRequired
    @RequestMapping(path = "/updatePassword", method = RequestMethod.POST)
    public String updatePassword(String oldPassword, String newPassword, String confirmPassword, Model model) {

        User user = hostHolder.getUser();
        if (user == null) {
            throw new RuntimeException("未登录用户试图修改密码!");
        }

        Map<String, Object> result = userService.updatePassword(oldPassword,newPassword,confirmPassword,user);
        if (result.isEmpty()) {
            return "redirect:/index";
        } else {
            model.addAttribute("oldPasswordMsg", result.get("oldPasswordMsg"));
            model.addAttribute("newPasswordMsg", result.get("newPasswordMsg"));
            model.addAttribute("confirmPasswordMsg", result.get("confirmPasswordMsg"));
            return "/site/setting";
        }
    }

    // 个人主页
    @RequestMapping(path = "/profile/{userId}", method = RequestMethod.GET)
    public String getProfilePage(@PathVariable("userId") int userId, Model model) {
        User user = userService.findUserById(userId);
        // 判断用户是否存在
        if (user == null) {
            throw new RuntimeException("用户不存在！");
        }

        model.addAttribute("user", user);
        int likeCount = likeService.findUserLikeCount(userId);
        model.addAttribute("likeCount",likeCount);
        // 查询关注数量
        long followeeCount = followService.findFolloweeCount(userId, ENTITY_TYPE_USER);
        model.addAttribute("followeeCount", followeeCount);
        // 查询粉丝数量
        long followerCount = followService.findFollowerCount(ENTITY_TYPE_USER, userId);
        model.addAttribute("followerCount",followerCount);

        // 当前是否已经关注这个人
        boolean hasFollowed = false;
        if (hostHolder.getUser() != null) {
            hasFollowed = followService.hasFollowed(hostHolder.getUser().getId(), ENTITY_TYPE_USER, userId);
        }
        model.addAttribute("hasFollowed", hasFollowed);

        return "/site/profile";
    }
}
