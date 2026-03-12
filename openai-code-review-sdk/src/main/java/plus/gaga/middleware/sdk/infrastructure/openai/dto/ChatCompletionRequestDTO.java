package plus.gaga.middleware.sdk.infrastructure.openai.dto;

import plus.gaga.middleware.sdk.domain.model.Model;

import java.util.List;

/**
 * 调用大模型聊天补全接口时使用的请求 DTO。
 */
public class ChatCompletionRequestDTO {

    // 默认模型直接给成 glm-4-flash，业务层也可以按需覆盖。
    private String model = Model.GLM_4_FLASH.getCode();
    private List<Prompt> messages;

    /**
     * 一条对话消息，最关键的两个字段就是角色和内容。
     */
    public static class Prompt {
        private String role;
        private String content;

        public Prompt() {
        }

        public Prompt(String role, String content) {
            this.role = role;
            this.content = content;
        }

        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public List<Prompt> getMessages() {
        return messages;
    }

    public void setMessages(List<Prompt> messages) {
        this.messages = messages;
    }
}
