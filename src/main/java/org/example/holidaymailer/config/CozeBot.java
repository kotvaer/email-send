package org.example.holidaymailer.config;

import com.coze.openapi.client.workflows.run.RunWorkflowReq;
import com.coze.openapi.client.workflows.run.RunWorkflowResp;
import com.coze.openapi.service.auth.TokenAuth;
import com.coze.openapi.service.service.CozeAPI;
import org.example.holidaymailer.tools.JsonParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Component
public class CozeBot {

    private final String token;
    private final String workflowId;
    private final String baseUrl;
    private final WebClient webClient;
    private final JsonParser jsonParser;
    private CozeAPI cozeAPI;  //  作为单例成员变量

    @Qualifier("virtualThreadExecutor")
    @Autowired
    Executor executor;

    // 构造注入
    public CozeBot(@Value("${coze.api.baseUrl}") String baseUrl,
                   @Value("${coze.api.token}") String token,
                   @Value("${coze.api.workflowId}") String workflowId,
                   WebClient.Builder webClientBuilder,
                   JsonParser jsonParser) {
        this.baseUrl = baseUrl;
        this.token = token;
        this.workflowId = workflowId;
        this.webClient = webClientBuilder.baseUrl(baseUrl).build();
        this.jsonParser = jsonParser;

        initCozeClient(); // 初始化 client
    }

    // 只初始化一次
    private void initCozeClient() {
        TokenAuth authCli = new TokenAuth(token);
        this.cozeAPI = new CozeAPI.Builder()
                .baseURL(baseUrl)
                .auth(authCli)
                .readTimeout(20000)
                .build();
    }


    private CompletableFuture<RunWorkflowResp> callAsync(Map<String, Object> data) {
        RunWorkflowReq req = RunWorkflowReq.builder()
                .workflowID(workflowId)
                .parameters(data)
                .build();
        return CompletableFuture.supplyAsync(() -> cozeAPI.workflows().runs().create(req),executor);
    }

    public CompletableFuture<RunWorkflowResp> callAsync(String name, String subject) {
        Map<String, Object> data = Map.of("name", name, "subject", subject);
        return callAsync(data);
    }

    public CompletableFuture<String> genContentAsync(String name, String subject) throws Exception {
        return callAsync(name, subject).thenApplyAsync(content -> {
                    try {
                        return jsonParser.getMessageFromJson(content.getData());
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }, executor);
    }

    // 保持异步调用方式不变，仍使用 WebClient
    private Mono<RunWorkflowResp> callReactive(Map<String, Object> data) {
        RunWorkflowReq request = RunWorkflowReq.builder()
                .workflowID(workflowId)
                .parameters(data)
                .build();
        return webClient.post()
                .uri("v1/workflow/run")
                .header("Authorization", "Bearer " + token)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(RunWorkflowResp.class);
    }

    public Mono<RunWorkflowResp> callReactive(String name, String subject) {
        Map<String, Object> data = Map.of("name", name, "subject", subject);
        return callReactive(data);
    }

    public Mono<String> genContentReactive(String name, String subject) throws Exception {
        return callReactive(name, subject).handle((json, sink) -> {
            try {
                sink.next(jsonParser.getMessageFromJson(json.getData()));
            } catch (Exception e) {
                sink.error(new RuntimeException(e));
            }
        });
    }
}
