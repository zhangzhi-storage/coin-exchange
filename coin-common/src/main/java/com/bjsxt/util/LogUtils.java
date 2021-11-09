package com.bjsxt.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

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
                // 发送消息
                ListenableFuture<SendResult<String, Object>> future = kafkaTemplate.send(kafkaTopic, jsonObj);
                future.addCallback(new ListenableFutureCallback<SendResult<String, Object>>() {
                    @Override
                    public void onFailure(Throwable throwable) {
                        log.info("Produce: The message failed to be sent:" + throwable.getMessage());
                    }

                    @Override
                    public void onSuccess(SendResult<String, Object> stringObjectSendResult) {
                        // TODO 业务处理
                        log.info("Produce: The message was sent successfully:");
                        log.info("Produce: _+_+_+_+_+_+_+ result: " + stringObjectSendResult.toString());
                    }
                });
            }
        });
    }
}
