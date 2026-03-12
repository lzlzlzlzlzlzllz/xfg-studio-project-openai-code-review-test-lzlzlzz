package plus.gaga.middleware.sdk.types.utils;

import java.util.Random;

/**
 * 简单随机串工具。
 * 当前主要用于生成评审日志文件名尾部的随机片段，降低重名概率。
 */
public class RandomStringUtils {

    public static String randomNumeric(int length) {
        // 虽然方法名叫 randomNumeric，但这里实际返回的是字母数字混合串。
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        Random random = new Random();
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(characters.charAt(random.nextInt(characters.length())));
        }
        return sb.toString();
    }

}
