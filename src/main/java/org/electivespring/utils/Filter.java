package org.electivespring.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.electivespring.database.entity.Course;
import org.electivespring.database.entity.StudentsSubtopic;
import org.electivespring.database.entity.Subtopic;
import org.electivespring.database.entity.User;
import org.electivespring.logic.StudentsSubtopicManager;
import org.electivespring.logic.SubtopicManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.electivespring.database.entity.StudentsSubtopic.completion.COMPLETED;
@Service
public class Filter {

    private static final Logger logger = LogManager.getLogger(Filter.class);

    @Autowired
    private SubtopicManager subtopicManager;

    @Autowired
    private StudentsSubtopicManager studentsSubtopicManager;

    /**
     * Filter topic filters list of courses that will be displayed by specified topics.
     *
     * @param topics  array of topics for which filtering courses
     * @param courses list of courses to filter
     * @return the list of courses after filtering
     */
    public List<Course> filterTopic(List<String> topics, List<Course> courses) {
        if (topics != null) {
            List<Course> result = new ArrayList<>();
            for (String topic : topics) {
                courses.stream()
                        .filter(c -> c.getTopic().equals(topic))
                        .forEach(result::add);
            }
            return result;
        }
        return courses;
    }

    /**
     * Filter teacher filters list of courses that will be displayed by specified teachers.
     *
     * @param teachers array of teachers for which filtering courses
     * @param courses  list of courses to filter
     * @return the list of courses after filtering
     */
    public List<Course> filterTeachers(List<String> teachers, List<Course> courses) {
        if (teachers != null) {
            List<Course> result = new ArrayList<>();
            for (String teacher : teachers) {
                courses.stream()
                        .filter(c -> c.getTeacherId() == Integer.parseInt(teacher))
                        .forEach(result::add);
            }
            return result;
        }
        return courses;
    }

    /**
     * Filter completions filters list of courses that will be displayed by specified completion.
     *
     * @param completions the completions of course by current student
     * @param courses     the courses of current student
     * @param user        current student
     * @return the list of courses after filtering
     */
    public List<Course> filterCompletions(List<String> completions, List<Course> courses, User user) {
        if (completions != null) {
            try {
                List<Course> result = new ArrayList<>();
                for (Course course : courses) {
                    List<Subtopic> subtopics = subtopicManager.findSubtopicsByCourse(course.getId());

                    List<StudentsSubtopic> studentsSubtopics = new ArrayList<>();
                    for (Subtopic subtopic : subtopics) {
                        studentsSubtopics.add(studentsSubtopicManager.findStudentsSubtopic(subtopic.getSubtopicId(), user.getId()));
                    }

                    List<StudentsSubtopic> finishedSubtopics = studentsSubtopics.stream()
                            .filter(s -> s.getCompletion().equals(COMPLETED.toString()))
                            .collect(Collectors.toList());

                    for (String completion : completions) {
                        checkCompletion(result, course, studentsSubtopics, finishedSubtopics, completion);
                    }

                }
                return result;
            } catch (Exception e) {
                logger.error(e);
                throw new RuntimeException(e);
            }
        }
        return courses;

    }

    private static void checkCompletion(List<Course> result, Course course, List<StudentsSubtopic> studentsSubtopics, List<StudentsSubtopic> finishedSubtopics, String completion) {
        if (completion.equals("0") && finishedSubtopics.isEmpty()) {
            result.add(course);
        }
        if (completion.equals("2") && finishedSubtopics.size() == studentsSubtopics.size() && !studentsSubtopics.isEmpty()) {
            result.add(course);
        }
        if (completion.equals("1") && !finishedSubtopics.isEmpty() && finishedSubtopics.size() < studentsSubtopics.size()) {
            result.add(course);
        }
    }

    public List<Course> filterName(String pattern, List<Course> courses) {
        return pattern != null ? courses.stream()
                .filter(course -> course.getName().toLowerCase().matches(".*" + pattern.toLowerCase() + ".*"))
                .collect(Collectors.toList()) : courses;
    }

}
