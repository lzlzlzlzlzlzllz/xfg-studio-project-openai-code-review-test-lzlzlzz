package plus.gaga.middleware.sdk.infrastructure.weixin;


import com.alibaba.fastjson2.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import plus.gaga.middleware.sdk.infrastructure.weixin.dto.TemplateMessageDTO;
import plus.gaga.middleware.sdk.types.utils.WXAccessTokenUtils;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Scanner;

/**
 * 微信模板消息发送器。
 * 主要职责是拿 access_token、组装模板消息并调用微信接口发出去。
 */
public class WeiXin {

    private final Logger logger = LoggerFactory.getLogger(WeiXin.class);

    private final String appid;

    private final String secret;

    private final String touser;

    private final String template_id;

    public WeiXin(String appid, String secret, String touser, String template_id) {
        this.appid = appid;
        this.secret = secret;
        this.touser = touser;
        this.template_id = template_id;
    }

    public void sendTemplateMessage(String logUrl, Map<String, Map<String, String>> data) throws Exception {
        // 先根据公众号 appid 和 secret 获取本次调用需要的 access_token。
        String accessToken = WXAccessTokenUtils.getAccessToken(appid, secret);

        TemplateMessageDTO templateMessageDTO = new TemplateMessageDTO(touser, template_id);
        templateMessageDTO.setUrl(logUrl);
        templateMessageDTO.setData(data);

        // 微信模板消息接口是标准 POST JSON 请求。
        URL url = new URL(String.format("https://api.weixin.qq.com/cgi-bin/message/template/send?access_token=%s", accessToken));
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json; utf-8");
        conn.setRequestProperty("Accept", "application/json");
        conn.setDoOutput(true);

        // 将模板消息实体序列化为 JSON。
        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = JSON.toJSONString(templateMessageDTO).getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        // 记录微信接口返回值，方便排查模板配置或鉴权问题。
        try (Scanner scanner = new Scanner(conn.getInputStream(), StandardCharsets.UTF_8.name())) {
            String response = scanner.useDelimiter("\\A").next();
            logger.info("openai-code-review weixin template message! {}", response);
        }
    }

}
