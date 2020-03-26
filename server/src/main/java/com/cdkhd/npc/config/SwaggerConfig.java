package com.cdkhd.npc.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.util.StopWatch;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.Collections;

import static springfox.documentation.builders.PathSelectors.regex;

@Configuration
@EnableSwagger2//注意这里
public class SwaggerConfig {

    @Autowired
    private Environment env;

    @Bean
    public Docket swaggerSpringfoxDocket4ManagerPlatform() {
        StopWatch watch = new StopWatch();
        watch.start();
        Docket swaggerSpringMvcPlugin = new Docket(DocumentationType.SWAGGER_2)
                .groupName("管理后台API")
                .apiInfo(apiInfo()).select().apis(RequestHandlerSelectors.any())
                .paths(regex("/api/manager/.*")) // and by paths
                .build();
        watch.stop();
        return swaggerSpringMvcPlugin;
    }

    @Bean
    public Docket swaggerSpringfoxDocket4MiniApp() {
        StopWatch watch = new StopWatch();
        watch.start();
        Docket swaggerSpringMvcPlugin = new Docket(DocumentationType.SWAGGER_2)
                .groupName("小程序API")
                .apiInfo(apiInfo()).select().apis(RequestHandlerSelectors.any())
                .paths(regex("/api/mobile/.*")) // and by paths
                .build();
        watch.stop();
        return swaggerSpringMvcPlugin;
    }

    @Bean
    public Docket swaggerSpringfoxDocket4DisplayPlatform() {
        StopWatch watch = new StopWatch();
        watch.start();
        Docket swaggerSpringMvcPlugin = new Docket(DocumentationType.SWAGGER_2)
                .groupName("展示平台API")
                .apiInfo(apiInfo()).select().apis(RequestHandlerSelectors.any())
                .paths(regex("/api/display/.*")) // and by paths
                .build();
        watch.stop();
        return swaggerSpringMvcPlugin;
    }

    private ApiInfo apiInfo() {//这里是生成文档基本信息的地方
        return new ApiInfo(env.getProperty("swagger.title"),
                env.getProperty("swagger.description"),
                env.getProperty("swagger.version"),
                env.getProperty("swagger.termsOfServiceUrl"),
                new Contact(
                        env.getProperty("swagger.contact.name"),
                        env.getProperty("swagger.contact.url"),
                        env.getProperty("swagger.contact.email"
                        )
                ),
                null,
                null, Collections.emptySet());
    }

}
