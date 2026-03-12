package plus.gaga.middleware.sdk;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import plus.gaga.middleware.sdk.domain.service.impl.OpenAiCodeReviewService;
import plus.gaga.middleware.sdk.infrastructure.git.GitCommand;
import plus.gaga.middleware.sdk.infrastructure.openai.IOpenAI;
import plus.gaga.middleware.sdk.infrastructure.openai.impl.ChatGLM;
import plus.gaga.middleware.sdk.infrastructure.weixin.WeiXin;

/**
 * SDK 启动入口。
 * 这个类不处理具体业务，只负责从环境变量组装依赖，然后触发一次完整的代码评审流程。
 */
public class OpenAiCodeReview {

    private static final Logger logger = LoggerFactory.getLogger(OpenAiCodeReview.class);

    // 下面这些字段更像是示例阶段留下的默认配置。
    // 当前主流程实际通过环境变量读取配置，因此这些成员变量并未参与运行时逻辑。
    // 学习时要注意：把 appid/secret 这类敏感信息硬编码在源码里并不是生产级写法。
    private String weixin_appid = "wx5a228ff69e28a91f";
    private String weixin_secret = "0bea03aa1310bac050aae79dd8703928";
    private String weixin_touser = "or0Ab6ivwmypESVp_bYuk92T6SvU";
    private String weixin_template_id = "l2HTkntHB71R4NQTW77UkcqvSOIFqE_bss1DAVQSybc";

    // ChatGLM 配置
    // 同样属于示例字段，真正运行时依赖的是环境变量里的值。
    private String chatglm_apiHost = "https://open.bigmodel.cn/api/paas/v4/chat/completions";
    private String chatglm_apiKeySecret = "";

    // Github 配置
    private String github_review_log_uri;
    private String github_token;

    // 工程配置 - 自动获取
    // 这些值来自 GitHub Actions 中提前写入的环境变量。
    private String github_project;
    private String github_branch;
    private String github_author;

    public static void main(String[] args) throws Exception {
        // GitCommand 负责两件事：
        // 1. 读取当前仓库最近一次提交的 diff
        // 2. 将 AI 评审结果写入评审日志仓库并推送
        GitCommand gitCommand = new GitCommand(
                getEnv("GITHUB_REVIEW_LOG_URI"),
                getEnv("GITHUB_TOKEN"),
                getEnv("COMMIT_PROJECT"),
                getEnv("COMMIT_BRANCH"),
                getEnv("COMMIT_AUTHOR"),
                getEnv("COMMIT_MESSAGE")
        );

        /**
         * 项目：{{repo_name.DATA}} 分支：{{branch_name.DATA}} 作者：{{commit_author.DATA}} 说明：{{commit_message.DATA}}
         */
        WeiXin weiXin = new WeiXin(
                getEnv("WEIXIN_APPID"),
                getEnv("WEIXIN_SECRET"),
                getEnv("WEIXIN_TOUSER"),
                getEnv("WEIXIN_TEMPLATE_ID")
        );

        // 这里通过 IOpenAI 做一层抽象，当前实现是接智谱 ChatGLM，
        // 以后如果想切换到其他兼容接口的模型，实现同一个接口即可。
        IOpenAI openAI = new ChatGLM(getEnv("CHATGLM_APIHOST"), getEnv("CHATGLM_APIKEYSECRET"));

        // 业务服务负责串起“取 diff -> AI 评审 -> 保存日志 -> 微信通知”整条链路。
        OpenAiCodeReviewService openAiCodeReviewService = new OpenAiCodeReviewService(gitCommand, openAI, weiXin);
        openAiCodeReviewService.exec();

        logger.info("openai-code-review done!");
    }

    private static String getEnv(String key) {
        String value = System.getenv(key);
        // 配置缺失时立即失败，避免后续在网络请求或 Git 操作时出现更难定位的错误。
        if (null == value || value.isEmpty()) {
            throw new RuntimeException("value is null");
        }
        return value;
    }

}
