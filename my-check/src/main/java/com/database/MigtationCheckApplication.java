package com.database;

import com.database.common.core.domain.AjaxResult;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.util.FileCopyUtils;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * 启动程序
 */
@SpringBootApplication
public class MigtationCheckApplication
{
    public static void main(String[] args)
    {
        // System.setProperty("spring.devtools.restart.enabled", "false");
        SpringApplication.run(MigtationCheckApplication.class, args);
        System.out.println( "    ____        __           ____  ____     __  ___      _____ ____    __ \n" +
                "   / __ \\____  / /___ ______/ __ \\/ __ )   /  |/  /_  __/ ___// __ \\  / / \n" +
                "  / /_/ / __ \\/ / __ `/ ___/ / / / __  |  / /|_/ / / / /\\__ \\/ / / / / /  \n" +
                " / ____/ /_/ / / /_/ / /  / /_/ / /_/ /  / /  / / /_/ /___/ / /_/ / / /___\n" +
                "/_/    \\____/_/\\__,_/_/  /_____/_____/  /_/  /_/\\__, //____/\\___\\_\\/_____/\n" +
                "                                               /____/  ");
        Resource resource = new ClassPathResource("statement.txt");
        try (InputStreamReader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8)) {
            // 将文件内容复制为字符串
            String content = FileCopyUtils.copyToString(reader);
            // 打印到控制台
            System.out.println(content);
        } catch (IOException e) {
            // 处理异常
            AjaxResult.error(e.getMessage());
        }
    }



}