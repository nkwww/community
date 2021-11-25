package com.nowcoder.community.event;

import com.alibaba.fastjson.JSONObject;
import com.nowcoder.community.entity.Event;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class EventProducer {

    @Autowired
    private KafkaTemplate kafkaTemplate;

    // 处理事件，发布消息
    public void fireEvent(Event event) {
        // 将事件发布到指定的主题
        // 不一个一个的填充具体内容，而是将event对象转为json，消费者拿到json再转为对象
        kafkaTemplate.send(event.getTopic(), JSONObject.toJSONString(event));
    }
}
