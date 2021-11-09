package com.bjsxt.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class LogUtils {
    @Autowired
    private static KafkaTemplate<String,String> kafkaTemplate;
    public static void sendLogMessage(String topic,String msg){
        ExecutorService threadPool = Executors.newFixedThreadPool(20);
        threadPool.execute(new Runnable() {
            @Override
            public void run() {
                kafkaTemplate.send(topic,"dm",msg);
            }
        });
    }
}
