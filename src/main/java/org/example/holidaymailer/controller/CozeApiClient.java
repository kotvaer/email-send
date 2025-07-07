package org.example.holidaymailer.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.util.HashMap;
import java.util.Map;

@Component
public class CozeApiClient {

    private final WebClient webClient;

    private final String apiUrl;

    private final String token;

    private final String workFlowId;

    public CozeApiClient(@Value("${coze.api.apiUrl}") String apiUrl,
                         @Value("${coze.api.token}") String token,
                         @Value("${coze.api.workFlowId}") String workFlowId) {
        this.apiUrl = apiUrl;
        this.token = token;
        this.workFlowId = workFlowId;

        this.webClient = WebClient.builder()
                .baseUrl(apiUrl)
                .defaultHeader("Authorization", token)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    public static CozeApiClient linked(String apiUrl, String token, String workFlowId) {
        return new CozeApiClient(apiUrl, token, workFlowId);
    }

    public Flux<String> callCozeApi(String name, String holiday, boolean birth) {
        // 构建请求体
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("birth", birth);
        parameters.put("holiday", holiday);
        parameters.put("name", name);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("workflow_id", workFlowId);
        requestBody.put("parameters", parameters);

        // 发送 POST 请求并处理流式响应
        return webClient.post()
                .bodyValue(requestBody)
                .retrieve()
                .bodyToFlux(String.class) // 假设响应是 SSE，每行一个字符串
                .onErrorMap(e -> new RuntimeException("Failed to call Coze API: " + e.getMessage(), e));
    }

//    public static void main(String[] args) {
//        CozeApiClient client = new CozeApiClient();
//        client.callCozeApi("7524227986921783342", "沙恒毅", "国庆节", true)
//                .subscribe(
//                        data -> System.out.println("Received: " + data),
//                        error -> System.err.println("Error: " + error.getMessage()),
//                        () -> System.out.println("Stream completed")
//                );
//    }
}
