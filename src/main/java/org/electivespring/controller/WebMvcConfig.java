package org.electivespring.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.electivespring.database.entity.User;
import org.electivespring.utils.Calculator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.spring6.view.ThymeleafViewResolver;

import java.io.IOException;
import java.util.Locale;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {


    @Bean(name = "localeResolver")
    public LocaleResolver getLocaleResolver()  {
        SessionLocaleResolver resolver= new SessionLocaleResolver();
        resolver.setDefaultLocale(new Locale("ru"));
        return resolver;
    }

    @Bean(name = "messageSource")
    public MessageSource getMessageResource()  {
        ReloadableResourceBundleMessageSource messageResource= new ReloadableResourceBundleMessageSource();

        // Read i18n/messages_xxx.properties file.
        // For example: i18n/messages_en.properties
        messageResource.setBasename("classpath:text");
        messageResource.setDefaultEncoding("UTF-8");
        return messageResource;
    }

    @Bean
    public ThymeleafViewResolver thymeleafViewResolver(@Autowired SpringTemplateEngine templateEngine ) {
        ThymeleafViewResolver thymeleafViewResolver = new ThymeleafViewResolver();
        thymeleafViewResolver.setTemplateEngine( templateEngine );
        thymeleafViewResolver.setCharacterEncoding( "UTF-8" );
        thymeleafViewResolver.addStaticVariable( "calculator", Calculator.getInstance());
        return thymeleafViewResolver;
    }
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        LocaleChangeInterceptor localeInterceptor = new LocaleChangeInterceptor();
        localeInterceptor.setParamName("lang");
        registry.addInterceptor(localeInterceptor).addPathPatterns("/*");

        HandlerInterceptor authorizationInterceptor = new HandlerInterceptor(){
            @Override
            public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
                int[] authorizedRoles = {1, 2, 3};
                HttpSession session = request.getSession();
                User user = (User) session.getAttribute("user");
                    for (int role : authorizedRoles) {
                        if (user != null && user.getRoleId() == role) {
                            return true;
                        }
                    }
                response.sendRedirect(request.getContextPath() + "/log_in");
                return false;
            }
        };
        registry.addInterceptor(authorizationInterceptor).addPathPatterns("/*").excludePathPatterns("/log_in", "/check_user", "/registration");
    }

}
