package com.nowcoder.community.community;

import com.nowcoder.community.CommunityApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeansException;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest
// 以CommunityApplication作为配置类环境启用测试类
@ContextConfiguration(classes = CommunityApplication.class)
class CommunityApplicationTests implements ApplicationContextAware {

	private ApplicationContext applicationContext;
	// applicationContext就是Spring容器
	// 一个类实现了ApplicationContextAware接口，sprig容器会检测到这个bean，然后调用下面这个set方法，把自身传进来，
	// 我们就可以在其他测试方法中使用Spring容器了
	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

	// 测试使用Spring 容器
	@Test
	public void testApplicationContext(){
		System.out.println(this.applicationContext);
	}
}
