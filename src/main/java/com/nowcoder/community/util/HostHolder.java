package com.nowcoder.community.util;

import com.nowcoder.community.entity.User;
import org.springframework.stereotype.Component;

/**
 * 持有用户信息，用于代替session对象。（我们不想用session对象过多地占用内存，session对象也是线程隔离的）
 */
@Component
public class HostHolder {

    private ThreadLocal<User> userThreadLocal = new ThreadLocal<>();

    public void setUser(User user) {
        userThreadLocal.set(user);
    }

    public User getUser() {
        return userThreadLocal.get();
    }

    // 请求结束时，清除ThreadLocal的值
    public void clear() {
        userThreadLocal.remove();
    }
}
