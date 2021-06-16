package de.tudresden.inf.st.bigraphs.editor.bigellor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.Map;
import java.util.function.BiFunction;

@SpringBootApplication
public class BigellorApplication {
    @Bean("pathGenerator")
    public BiFunction<String, Map<String, Boolean>, String> pathGenerator() {
        return new BiFunction<String, Map<String, Boolean>, String>() {
            @Override
            public String apply(String forNavItemId, Map<String, Boolean> s) {
                if (s.get(forNavItemId)) {
                    return "";
                }
                return "disabled";
            }
        };
    }



    public static void main(String[] args) {
        SpringApplication.run(BigellorApplication.class, args);
    }

}
