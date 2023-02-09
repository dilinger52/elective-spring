package org.electivespring.logic;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.electivespring.database.dao.CourseRepository;
import org.electivespring.database.entity.Course;
import org.electivespring.database.entity.StudentsCourse;
import org.electivespring.database.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CourseManager {
    private static final Logger logger = LogManager.getLogger(CourseManager.class);
    private static final String COURSE_MESSAGE = "course: {}";

    @Autowired
    private final CourseRepository courseRepository;
    @Autowired
    private StudentsCourseManager studentsCourseManager;
    @Autowired
    private UserManager userManager;

    public CourseManager(CourseRepository courseRepository) {
        this.courseRepository = courseRepository;
    }
    public Course findCourseById(int courseId) throws SQLException {
        Optional<Course> optCourse = courseRepository.findById(courseId);
        if (optCourse.isPresent()) {
            Course course = optCourse.get();
            logger.debug("course properties: {}", course);
            return course;
        }
        throw new SQLException();
    }

    public List<Course> findCoursesByName(String pattern) throws SQLException {
        List<Course> listCourse = courseRepository.findByName(pattern);
        if (!listCourse.isEmpty()) {
            logger.debug("courses properties: {}", listCourse);
            return listCourse;
        }
        throw new SQLException();
    }

    public List<Course> findAllCourses() throws SQLException {
        Iterable<Course> iterable = courseRepository.findAll();
        List<Course> listCourse = new ArrayList<>();
        iterable.forEach(listCourse::add);
        if (!listCourse.isEmpty()) {
            logger.debug("courses properties: {}", listCourse);
            return listCourse;
        }
        throw new SQLException();
    }

    public List<Course> getAvailableCourses(User user, List<Course> courses) throws SQLException {
        List<Course> coursesByStudent = findCoursesByStudent(user.getId());
        List<Integer> sc = new ArrayList<>();
        coursesByStudent.forEach(c -> sc.add(c.getId()));
        return courses.stream().filter(c -> !(sc.contains(c.getId()))).collect(Collectors.toList());
    }

    public List<Course> findCoursesByStudent(int id) throws SQLException {
        List<Course> courses = new ArrayList<>();
        List<StudentsCourse> studentsCourses = studentsCourseManager.findCoursesByStudent(id);
        for (StudentsCourse c : studentsCourses) {
            Course course = findCourseById(c.getCourseId());
            courses.add(course);
        }
        logger.debug("courses properties: {}", courses);
        return courses;
    }

    public List<Course> findCoursesByTeacher (int id) throws SQLException {
        List<Course> listCourse = courseRepository.findByTeacherId(id);
        if (listCourse != null) {
            logger.debug("courses properties: {}", listCourse);
            return listCourse;
        }
        throw new SQLException();
    }

    public List<String> findAllTopics() throws SQLException {
        List<String> listCourse = courseRepository.findAllTopics();
        List<String> result = new ArrayList<>();
        for (String topic : listCourse) {
            if (!result.contains(topic)) {
                result.add(topic);
            }
        }
        if (!result.isEmpty()) {
            logger.debug("courses properties: {}", listCourse);
            return result;
        }
        throw new SQLException();
    }

    public Map<Integer, String> getCoursesTeacher(List<Course> courses) throws SQLException {
        Map<Integer, String> coursesTeacher = new HashMap<>();
        for (Course course : courses) {
            User teacher = userManager.findUserById(course.getTeacherId());
            coursesTeacher.put(course.getId(), teacher.getFirstName() + " " +
                    teacher.getLastName());
        }
        return coursesTeacher;
    }

    public Map<Integer, Integer> getCoursesStudents(List<Course> courses) {
        Map<Integer, Integer> coursesStudents = new HashMap<>();
        for (Course course : courses) {
            coursesStudents.put(course.getId(), studentsCourseManager.findCoursesByCourseId(course.getId()).size());
        }
        return coursesStudents;
    }

    public void updateCourse(int id, String name, String topic, String description, int teacherId, long duration) throws SQLException {
        Optional<Course> optCourse = courseRepository.findById(id);
        if (optCourse.isEmpty()) {
            throw new SQLException();
        }
        Course course = optCourse.get();
        course.setName(name);
        course.setTopic(topic);
        course.setDescription(description);
        course.setTeacherId(teacherId);
        course.setDuration(duration);
        courseRepository.save(course);
        logger.debug(COURSE_MESSAGE, course);
    }
    public void insertCourse(Course course) {
        courseRepository.save(course);
        logger.debug(COURSE_MESSAGE, course);
    }

    public void deleteCourse(int id) {
        courseRepository.deleteById(id);
        logger.debug("Successfully deleting");
    }
}
