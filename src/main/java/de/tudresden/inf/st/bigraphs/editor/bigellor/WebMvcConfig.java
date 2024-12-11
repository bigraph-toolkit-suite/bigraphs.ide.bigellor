package de.tudresden.inf.st.bigraphs.editor.bigellor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.servlet.config.annotation.AsyncSupportConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
//@EnableWebFlux
public class WebMvcConfig implements WebMvcConfigurer { //WebFluxConfigurer { //

    @Bean
    public ThreadPoolTaskExecutor mvcTaskExecutor() {
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setCorePoolSize(Runtime.getRuntime().availableProcessors());
        taskExecutor.setMaxPoolSize(Runtime.getRuntime().availableProcessors()*4);
        return taskExecutor;
    }

    public void configureAsyncSupport(AsyncSupportConfigurer configurer) {
        configurer.setTaskExecutor(mvcTaskExecutor());
    }

//    @Bean
//    public ITemplateResolver thymeleafTemplateResolver() {
//        final SpringResourceTemplateResolver resolver = new SpringResourceTemplateResolver();
////        resolver.setApplicationContext(this.context);
//        resolver.setPrefix("classpath:templates/");
//        resolver.setSuffix(".html");
//        resolver.setTemplateMode(TemplateMode.HTML);
//        resolver.setCacheable(false);
//        resolver.setCheckExistence(false);
//        return resolver;
//    }
//
//
//    @Bean
//    public ISpringWebFluxTemplateEngine thymeleafTemplateEngine() {
//        SpringWebFluxTemplateEngine templateEngine = new SpringWebFluxTemplateEngine();
//        templateEngine.setTemplateResolver(thymeleafTemplateResolver());
//        return templateEngine;
//    }
//
//    @Bean
//    public ThymeleafReactiveViewResolver thymeleafReactiveViewResolver() {
//        ThymeleafReactiveViewResolver viewResolver = new ThymeleafReactiveViewResolver();
//        viewResolver.setTemplateEngine(thymeleafTemplateEngine());
//        return viewResolver;
//    }
//
//    @Override
//    public void configureViewResolvers(ViewResolverRegistry registry) {
//        registry.viewResolver(thymeleafReactiveViewResolver());
//    }
}
