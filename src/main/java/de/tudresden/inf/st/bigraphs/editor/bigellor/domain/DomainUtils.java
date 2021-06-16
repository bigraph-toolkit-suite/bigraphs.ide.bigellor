package de.tudresden.inf.st.bigraphs.editor.bigellor.domain;

import com.github.javafaker.Faker;

public class DomainUtils {
    static Faker faker = new Faker();

    public static String createPlaceholderName() {
        String slug = faker.internet().slug();
        if (slug.contains("_")) {
            slug = slug.split("_")[0];
        }
        return slug;
    }
}
