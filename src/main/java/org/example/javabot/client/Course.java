package org.example.javabot.client;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public enum Course {
    FIRST_COURSE("1course.xlsx", "1 курс"),
    SECOND_COURSE("2course.xlsx", "2 курс"),
    THIRD_COURSE("3course.xlsx", "3 курс");

    private final String fileName;
    private final String displayName;

    Course(String fileName, String displayName) {
        this.fileName = fileName;
        this.displayName = displayName;
    }

    public String getFileName() {
        return fileName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public File getFile() {
        return new File("courses/" + fileName);
    }

    // Получить все курсы
    public static List<Course> getAllCourses() {
        return Arrays.asList(values());
    }
}
