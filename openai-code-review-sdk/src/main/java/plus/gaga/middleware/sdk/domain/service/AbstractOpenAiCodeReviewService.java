package plus.gaga.middleware.sdk.domain.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import plus.gaga.middleware.sdk.infrastructure.git.GitCommand;
import plus.gaga.middleware.sdk.infrastructure.openai.IOpenAI;
import plus.gaga.middleware.sdk.infrastructure.weixin.WeiXin;

import java.io.IOException;


/**
 * 代码评审流程模板。
 * 子类只关心每一步怎么做，整体执行顺序由这个抽象类统一控制。
 */
public abstract class AbstractOpenAiCodeReviewService implements IOpenAiCodeReviewService {

    private final Logger logger = LoggerFactory.getLogger(AbstractOpenAiCodeReviewService.class);

    protected final GitCommand gitCommand;
    protected final IOpenAI openAI;
    protected final WeiXin weiXin;

    public AbstractOpenAiCodeReviewService(GitCommand gitCommand, IOpenAI openAI, WeiXin weiXin) {
        this.gitCommand = gitCommand;
        this.openAI = openAI;
        this.weiXin = weiXin;
    }

    @Override
    public void exec() {
        try {
            // 1. 读取本次提交差异，作为后续 AI 评审的输入
            String diffCode = getDiffCode();
            // 2. 把 diff 发给大模型，生成代码评审建议
            String recommend = codeReview(diffCode);
            // 3. 将评审结果写入日志仓库，并拿到可访问的日志地址
            String logUrl = recordCodeReview(recommend);
            // 4. 把日志地址和提交信息包装后发送通知
            pushMessage(logUrl);
        } catch (Exception e) {
            logger.error("openai-code-review error", e);
        }

    }

    protected abstract String getDiffCode() throws IOException, InterruptedException;

    protected abstract String codeReview(String diffCode) throws Exception;

    protected abstract String recordCodeReview(String recommend) throws Exception;

    protected abstract void pushMessage(String logUrl) throws Exception;

}
