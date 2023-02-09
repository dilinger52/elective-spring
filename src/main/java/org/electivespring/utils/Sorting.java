package org.electivespring.utils;

import jakarta.servlet.http.HttpSession;
import org.electivespring.database.entity.Course;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Utility class that contains method to sort courses in the list by different patterns
 */
public class Sorting {
    /**
     * Private constructor that prevents creating instance of this class
     */
    private Sorting() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Sorts list of courses by pattern
     *
     * @param session current session that contains information about students registered to courses
     * @param courses list of courses that need to be sorted
     * @param pattern changes the order of sorted list:
     *                0 - default order, by course name from A to Z;
     *                1 - by students registered to course from low to high;
     *                2 - by students registered to course from high to low;
     *                3 - by course name from A to Z;
     *                4 - by course name from Z to A;
     *                5 - by course duration from low to high;
     *                6 - by course duration from high to low;
     * @return inserted list sorted in specified order
     */
    public static List<Course> sort(HttpSession session, List<Course> courses, String pattern) {
        if (pattern != null) {
            switch (pattern) {
                case "1":
                    courses = courses.stream().sorted((o1, o2) -> {
                        Map<Integer, Integer> coursesStudents = (Map<Integer, Integer>) session.getAttribute("coursesStudents");
                        return coursesStudents.get(o1.getId()).compareTo(coursesStudents.get(o2.getId()));
                    }).collect(Collectors.toList());
                    break;
                case "2":
                    courses = courses.stream().sorted((o1, o2) -> {
                        Map<Integer, Integer> coursesStudents = (Map<Integer, Integer>) session.getAttribute("coursesStudents");
                        return -1 * (coursesStudents.get(o1.getId()).compareTo(coursesStudents.get(o2.getId())));
                    }).collect(Collectors.toList());
                    break;
                default:
                case "0":
                case "3":
                    courses = courses.stream()
                            .sorted((o1, o2) -> (o1.getName().compareToIgnoreCase(o2.getName())))
                            .collect(Collectors.toList());
                    break;
                case "4":
                    courses = courses.stream()
                            .sorted((o1, o2) -> -1 * (o1.getName().compareToIgnoreCase(o2.getName())))
                            .collect(Collectors.toList());
                    break;
                case "5":
                    courses = courses.stream()
                            .sorted(Comparator.comparing(Course::getDuration))
                            .collect(Collectors.toList());
                    break;
                case "6":
                    courses = courses.stream()
                            .sorted((o1, o2) -> -1 * (String.valueOf(o1.getDuration()).compareTo(String.valueOf(o2.getDuration()))))
                            .collect(Collectors.toList());
                    break;
            }
        }

        return courses;
    }
}
