package com.nowcoder.community.util;

import org.apache.commons.lang3.StringUtils;
import org.springframework.util.DigestUtils;

import java.util.UUID;

public class CommunityUtil {

    // 生成随机字符串
    public static String generateUUID() {
        // 把 - 替换为空字符串
        return UUID.randomUUID().toString().replaceAll("-", "");
    }

    // MD5加密，防止被DBA泄露或者黑客盗取
    // 一个输入只有一个唯一的输出，可以被密码本破解
    // 通过加盐的方式 password + salt -> 加密结果
    public static String md5(String key) {
        if (StringUtils.isBlank(key)) {
            return null;
        }
        return DigestUtils.md5DigestAsHex(key.getBytes());
    }

}
