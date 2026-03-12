package plus.gaga.middleware.sdk.domain.service;

/**
 * 代码评审服务顶层接口。
 * 对外暴露的能力非常简单：执行一次完整评审。
 */
public interface IOpenAiCodeReviewService {

    void exec();

}
