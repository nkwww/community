package com.nowcoder.community.config;

import com.nowcoder.community.quartz.AlphaJob;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;
import org.springframework.scheduling.quartz.SimpleTriggerFactoryBean;

// 配置 -》 第一次执行写入数据库 -》 后续调用都从数据库中查询
@Configuration
public class QuartzConfig {

    // beanFactory  spring ioc 容器的顶层接口

    // factoryBean 可简化bean的实例化过程
    // 1.spring通过factoryBean封装了bean的实例化过程
    // 2.可以将factoryBean装配到Spring容器中
    // 3.将factoryBean注入给其他的bean
    // 4.其他的bean可以得到factoryBean所管理的对象实例

    // 配置JobDetail
    // @Bean
    public JobDetailFactoryBean alphaJobDetail() {
        JobDetailFactoryBean jobDetailFactoryBean = new JobDetailFactoryBean();
        jobDetailFactoryBean.setJobClass(AlphaJob.class);
        jobDetailFactoryBean.setName("alphaJob");
        jobDetailFactoryBean.setGroup("alphaJobGroup");
        jobDetailFactoryBean.setDurability(true);   // 任务是持久的保存吗
        jobDetailFactoryBean.setRequestsRecovery(true); // 任务是否可恢复
        return jobDetailFactoryBean;
    }

    // 配置Trigger触发器
    // @Bean
    public SimpleTriggerFactoryBean simpleTriggerFactoryBean(JobDetail alphaJobDetail) {
        SimpleTriggerFactoryBean simpleTriggerFactoryBean = new SimpleTriggerFactoryBean();
        simpleTriggerFactoryBean.setJobDetail(alphaJobDetail);
        simpleTriggerFactoryBean.setName("alphaTrigger");
        simpleTriggerFactoryBean.setGroup("alphaTriggerGroup");
        simpleTriggerFactoryBean.setRepeatInterval(3000);   // 触发频率
        simpleTriggerFactoryBean.setJobDataMap(new JobDataMap());
        return simpleTriggerFactoryBean;
    }
}
