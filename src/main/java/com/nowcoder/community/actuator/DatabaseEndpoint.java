package com.nowcoder.community.actuator;

import com.nowcoder.community.util.CommunityUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

@Component
@Endpoint(id = "database")
public class DatabaseEndpoint {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseEndpoint.class);

    // 通过连接池访问数据库
    @Autowired
    private DataSource dataSource;

    // 只能通过get方式访问这个端点
    @ReadOperation
    public String checkOperation() {
        try(Connection coon = dataSource.getConnection();

        ) {
            return CommunityUtil.getJsonString(0, "获取连接成功！");
        }catch (SQLException e) {
            logger.error("获取连接失败： " + e.getMessage());
            return CommunityUtil.getJsonString(1, "获取连接失败！");
        }
    }
}
