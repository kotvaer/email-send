package org.example.holidaymailer.config;

import com.coze.openapi.client.workflows.run.RunWorkflowReq;
import com.coze.openapi.client.workflows.run.RunWorkflowResp;
import com.coze.openapi.service.auth.TokenAuth;
import com.coze.openapi.service.service.CozeAPI;
import org.example.holidaymailer.tools.JsonParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class CozeBot {

    @Value("${coze.api.token}")
    private String token;

    @Value("${coze.api.workflowId}")
    private String workflowId;

    @Value("${coze.api.baseUrl}")
    private String baseUrl;

    @Autowired
    private JsonParser jsonParser;

    private RunWorkflowResp call(Map<String, Object> data) {
        // Get an access_token through personal access token or oauth.
        TokenAuth authCli = new TokenAuth(token);
        // Init the Coze client through the access_token.
        CozeAPI coze = new CozeAPI.Builder()
                .baseURL(baseUrl)
                .auth(authCli)
                .readTimeout(10000 * 2)
                .build();
        // if your workflow need input params, you can send them by map
        RunWorkflowReq.RunWorkflowReqBuilder<?, ?> builder = RunWorkflowReq.builder();
        builder.workflowID(workflowId).parameters(data);
        RunWorkflowResp resp = coze.workflows().runs().create(builder.build());
        return resp;
    }


    public RunWorkflowResp call(String name, String subject) {
        Map<String, Object> data = new HashMap<>();
        data.put("name", name);
        data.put("subject", subject);
        return call(data);
    }

    public String genContent(String name, String subject) throws Exception {
        Map<String, Object> data = new HashMap<>();
        data.put("name", name);
        data.put("subject", subject);
        return jsonParser.getMessageFromJson(call(data).getData());
    }

}
