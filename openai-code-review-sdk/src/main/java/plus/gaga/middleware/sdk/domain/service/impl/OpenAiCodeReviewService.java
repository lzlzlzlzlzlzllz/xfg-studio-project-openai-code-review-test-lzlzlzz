package plus.gaga.middleware.sdk.domain.service.impl;


import plus.gaga.middleware.sdk.domain.model.Model;
import plus.gaga.middleware.sdk.domain.service.AbstractOpenAiCodeReviewService;
import plus.gaga.middleware.sdk.infrastructure.git.GitCommand;
import plus.gaga.middleware.sdk.infrastructure.openai.IOpenAI;
import plus.gaga.middleware.sdk.infrastructure.openai.dto.ChatCompletionRequestDTO;
import plus.gaga.middleware.sdk.infrastructure.openai.dto.ChatCompletionSyncResponseDTO;
import plus.gaga.middleware.sdk.infrastructure.weixin.WeiXin;
import plus.gaga.middleware.sdk.infrastructure.weixin.dto.TemplateMessageDTO;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * 默认代码评审实现。
 * 这里把模板类中定义的 4 个步骤落地为具体行为。
 */
public class OpenAiCodeReviewService extends AbstractOpenAiCodeReviewService {

    public OpenAiCodeReviewService(GitCommand gitCommand, IOpenAI openAI, WeiXin weiXin) {
        super(gitCommand, openAI, weiXin);
    }

    @Override
    protected String getDiffCode() throws IOException, InterruptedException {
        // 直接委托给 GitCommand，从当前仓库提取最近一次提交的 diff。
        return gitCommand.diff();
    }

    @Override
    protected String codeReview(String diffCode) throws Exception {
        ChatCompletionRequestDTO chatCompletionRequest = new ChatCompletionRequestDTO();
        chatCompletionRequest.setModel(Model.GLM_4_FLASH.getCode());
        // 这里拼装给大模型的 messages。
        // 第一段是系统化的评审要求，第二段才是真正要被审查的 diff 内容。
        chatCompletionRequest.setMessages(new ArrayList<ChatCompletionRequestDTO.Prompt>() {
            private static final long serialVersionUID = -7988151926241837899L;

            {
                // 这段超长 prompt 决定了模型输出的风格、结构和关注点，
                // 也是整个项目“评审质量”最核心的提示词资产。
                add(new ChatCompletionRequestDTO.Prompt("user", "你是一位资深编程专家，拥有深厚的编程基础和广泛的技术栈知识。你的专长在于识别代码中的低效模式、安全隐患、以及可维护性问题，并能提出针对性的优化策略。你擅长以易于理解的方式解释复杂的概念，确保即使是初学者也能跟随你的指导进行有效改进。在提供优化建议时，你注重平衡性能、可读性、安全性、逻辑错误、异常处理、边界条件，以及可维护性方面的考量，同时尊重原始代码的设计意图。\n" +
                        "你总是以鼓励和建设性的方式提出反馈，致力于提升团队的整体编程水平，详尽指导编程实践，雕琢每一行代码至臻完善。用户会将仓库代码分支修改代码给你，以git diff 字符串的形式提供，你需要根据变化的代码，帮忙review本段代码。然后你review内容的返回内容必须严格遵守下面我给你的格式，包括标题内容。\n" +
                        "模板中的变量内容解释：\n" +
                        "变量1是给review打分，分数区间为0~100分。\n" +
                        "变量2 是code review发现的问题点，包括：可能的性能瓶颈、逻辑缺陷、潜在问题、安全风险、命名规范、注释、以及代码结构、异常情况、边界条件、资源的分配与释放等等\n" +
                        "变量3是具体的优化修改建议。\n" +
                        "变量4是你给出的修改后的代码。 \n" +
                        "变量5是代码中的优点。\n" +
                        "变量6是代码的逻辑和目的，识别其在特定上下文中的作用和限制\n" +
                        "\n" +
                        "必须要求：\n" +
                        "1. 以精炼的语言、严厉的语气指出存在的问题。\n" +
                        "2. 你的反馈内容必须使用严谨的markdown格式\n" +
                        "3. 不要携带变量内容解释信息。\n" +
                        "4. 有清晰的标题结构\n" +
                        "返回格式严格如下：\n" +
                        "# 小傅哥项目： OpenAi 代码评审.\n" +
                        "### \uD83D\uDE00代码评分：{变量1}\n" +
                        "#### \uD83D\uDE00代码逻辑与目的：\n" +
                        "{变量6}\n" +
                        "#### ✅代码优点：\n" +
                        "{变量5}\n" +
                        "#### \uD83E\uDD14问题点：\n" +
                        "{变量2}\n" +
                        "#### \uD83C\uDFAF修改建议：\n" +
                        "{变量3}\n" +
                        "#### \uD83D\uDCBB修改后的代码：\n" +
                        "{变量4}\n" +
                        "`;代码如下:"));
                // 将真实 diff 作为第二条消息输入模型。
                add(new ChatCompletionRequestDTO.Prompt("user", diffCode));
            }
        });

        // 发起同步请求，直接取第一条候选答案作为最终评审结果。
        ChatCompletionSyncResponseDTO completions = openAI.completions(chatCompletionRequest);
        ChatCompletionSyncResponseDTO.Message message = completions.getChoices().get(0).getMessage();
        return message.getContent();
    }

    @Override
    protected String recordCodeReview(String recommend) throws Exception {
        // 评审结果会被写成 markdown 文件，并提交到单独的日志仓库。
        return gitCommand.commitAndPush(recommend);
    }

    @Override
    protected void pushMessage(String logUrl) throws Exception {
        // 模板消息 data 的结构必须符合微信模板消息要求：
        // { key: { value: "xxx" } }
        Map<String, Map<String, String>> data = new HashMap<>();
        TemplateMessageDTO.put(data, TemplateMessageDTO.TemplateKey.REPO_NAME, gitCommand.getProject());
        TemplateMessageDTO.put(data, TemplateMessageDTO.TemplateKey.BRANCH_NAME, gitCommand.getBranch());
        TemplateMessageDTO.put(data, TemplateMessageDTO.TemplateKey.COMMIT_AUTHOR, gitCommand.getAuthor());
        TemplateMessageDTO.put(data, TemplateMessageDTO.TemplateKey.COMMIT_MESSAGE, gitCommand.getMessage());
        weiXin.sendTemplateMessage(logUrl, data);
    }

}
