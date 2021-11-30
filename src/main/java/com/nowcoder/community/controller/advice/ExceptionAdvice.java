package com.nowcoder.community.controller.advice;

import com.nowcoder.community.util.CommunityUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

// ControllerAdvice默认会去扫描所有的bean，可以加以限制，只扫描带有@Controller注解的bean
@ControllerAdvice(annotations = Controller.class)
public class ExceptionAdvice {

    private static final Logger logger = LoggerFactory.getLogger(ExceptionAdvice.class);

    // ExceptionHandler 处理指定的异常
    @ExceptionHandler({Exception.class})
    public void handlerException(Exception e, HttpServletRequest request, HttpServletResponse response) throws IOException {
        logger.error("服务器发生异常： " + e.getMessage());
        for (StackTraceElement stackTraceElement : e.getStackTrace()) {
            logger.error(stackTraceElement.toString());
        }

        // 判断请求是异步还是同步
        String xRequestedWith = request.getHeader("x-requested-with");
        if ("XMLHttpRequest".equals(xRequestedWith)) {
            // 异步请求
//            response.setContentType("application/json");      向浏览器返回一个字符串，浏览器会自动将其转为对象
            response.setContentType("application/plain;charset=utf-8"); // 向浏览器返回一个普通字符串，可以是json格式，浏览器需要人为转换为对象
            PrintWriter writer = response.getWriter();
            writer.write(CommunityUtil.getJsonString(1, "服务器异常"));
        } else {
            // 重定向到错误页面
            response.sendRedirect(request.getContextPath() + "/error");
        }
    }

}
