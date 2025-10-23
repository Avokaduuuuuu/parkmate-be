package com.parkmate.fcm;

import com.google.firebase.messaging.BatchResponse;

import java.util.List;
import java.util.Map;

public interface FCMService {

    String sendToDevice(String token, String title, String body, Map<String, String> data);

    BatchResponse sendToMultipleDevices(List<String> tokens, String title, String body, Map<String, String> data);

    String sendToTopic(String topic, String title, String body, Map<String, String> data);
}
