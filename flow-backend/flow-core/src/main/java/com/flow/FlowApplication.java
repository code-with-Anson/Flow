package com.flow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(exclude = {
        org.springframework.ai.autoconfigure.openai.OpenAiAutoConfiguration.class
        // 这里樱和晓帮我禁用了OPENAI自带的自动装配，因为我们需要使用dynamicAiFactory动态创建Client
})
public class FlowApplication {

    public static void main(String[] args) {
        var context = SpringApplication.run(FlowApplication.class, args);
        var env = context.getEnvironment();
        String port = env.getProperty("server.port", "8080");
        String contextPath = env.getProperty("server.servlet.context-path", "");
        String host = "localhost";

        System.out.println("\n----------------------------------------------------------");
        System.out.println("\t心流的后端已经成功启动! 请访问这里查看接口:");
        System.out.println("\tLocal: http://" + host + ":" + port + contextPath + "/doc.html");
        System.out.println("----------------------------------------------------------\n");
    }

}
