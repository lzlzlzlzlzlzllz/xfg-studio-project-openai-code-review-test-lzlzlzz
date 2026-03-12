package plus.gaga.middleware.sdk.infrastructure.openai.dto;

import java.util.List;

/**
 * 大模型同步返回结果 DTO。
 * 当前项目只关心第一条 choice 里的 message.content。
 */
public class ChatCompletionSyncResponseDTO {

    private List<Choice> choices;

    public static class Choice {
        private Message message;

        public Message getMessage() {
            return message;
        }

        public void setMessage(Message message) {
            this.message = message;
        }
    }

    public static class Message {
        private String role;
        private String content;

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

    public List<Choice> getChoices() {
        return choices;
    }

    public void setChoices(List<Choice> choices) {
        this.choices = choices;
    }
}
