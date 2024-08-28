//package com.database.framework.config;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
//import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
//import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
//import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
//import com.database.common.config.MigtationCheckConfig;
//import com.database.common.constant.Constants;
//import com.database.framework.interceptor.RepeatSubmitInterceptor;
//
///**
// * 通用配置
//
// */
//@Configuration
//public class ResourcesConfig implements WebMvcConfigurer
//{
//    /**
//     * 首页地址
//     */
//    private String indexUrl;
//
//    @Autowired
//    private RepeatSubmitInterceptor repeatSubmitInterceptor;
//
//    /**
//     * 默认首页的设置，当输入域名是可以自动跳转到默认指定的网页
//     */
//    @Override
//    public void addViewControllers(ViewControllerRegistry registry)
//    {
//
//    }
//
//    @Override
//    public void addResourceHandlers(ResourceHandlerRegistry registry)
//    {
//        /** 本地文件上传路径 */
//        registry.addResourceHandler(Constants.RESOURCE_PREFIX + "/**").addResourceLocations("file:" + MigtationCheckConfig.getProfile() + "/");
//
//        /** swagger配置 */
//        registry.addResourceHandler("/swagger-ui/**").addResourceLocations("classpath:/META-INF/resources/webjars/springfox-swagger-ui/");
//    }
//
//    /**
//     * 自定义拦截规则
//     */
//    @Override
//    public void addInterceptors(InterceptorRegistry registry)
//    {
//        registry.addInterceptor(repeatSubmitInterceptor).addPathPatterns("/**");
//    }
//}