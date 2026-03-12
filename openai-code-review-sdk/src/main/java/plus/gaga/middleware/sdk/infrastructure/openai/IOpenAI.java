package plus.gaga.middleware.sdk.infrastructure.openai;


import plus.gaga.middleware.sdk.infrastructure.openai.dto.ChatCompletionRequestDTO;
import plus.gaga.middleware.sdk.infrastructure.openai.dto.ChatCompletionSyncResponseDTO;

/**
 * 大模型调用抽象。
 * 业务层依赖这个接口，而不是直接依赖具体模型厂商实现。
 */
public interface IOpenAI {

    ChatCompletionSyncResponseDTO completions(ChatCompletionRequestDTO requestDTO) throws Exception;

}
