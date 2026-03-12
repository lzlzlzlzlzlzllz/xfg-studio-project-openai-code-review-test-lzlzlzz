package plus.gaga.middleware.test;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * 演示用测试类。
 * 这里故意制造一个明显错误，方便生成 diff 后让代码评审系统去识别问题。
 */
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class ApiTest {

    @Test
    public void test() {
        // 这里会触发 NumberFormatException，是一个故意构造的坏例子。
        System.out.println(Integer.parseInt("aaaaaa"));

    }

}
