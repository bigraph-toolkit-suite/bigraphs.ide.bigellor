package de.tudresden.inf.st.bigraphs.editor.bigellor.domain;

import com.github.javafaker.Faker;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

public class DomainUtils {
    static Faker faker = new Faker();

    public static String createPlaceholderName() {
        String slug = faker.internet().slug();
        if (slug.contains("_")) {
            slug = slug.split("_")[0];
        }
        return slug;
    }


    public static void writeProjectFile(String file, String projectName) {
        FileWriter myWriter;
        try {
            myWriter = new FileWriter(file);
            myWriter.write(projectName);
            myWriter.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String readProjectFile(String file) {
        String data = "";
        try {
            File myObj = new File(file);
            Scanner myReader = new Scanner(myObj);
            while (myReader.hasNextLine()) {
                data = myReader.nextLine();
                break;
            }
            myReader.close();
            return data;
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
        return data;
    }
}
