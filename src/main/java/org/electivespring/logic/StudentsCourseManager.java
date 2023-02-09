package org.electivespring.logic;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.electivespring.database.dao.*;
import org.electivespring.database.entity.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLException;
import java.util.*;

@Service
public class StudentsCourseManager {

    private static final Logger logger = LogManager.getLogger(StudentsCourseManager.class);

    @Autowired
    private final StudentsCourseRepository studentsCourseRepository;
    @Autowired
    private SubtopicManager subtopicManager;
    @Autowired
    private StudentsSubtopicRepository studentsSubtopicRepository;
    public StudentsCourseManager(StudentsCourseRepository studentsCourseRepository) {
        this.studentsCourseRepository = studentsCourseRepository;
    }

    public StudentsCourse findStudentsCourse(int courseId, int studentId) throws SQLException {
            Optional<StudentsCourse> optResult = studentsCourseRepository.findByCourseIdAndStudentId(courseId, studentId);
            if (optResult.isEmpty()) {
                throw new SQLException();
            }
            StudentsCourse result = optResult.get();
            logger.debug("find course: {}", result);
            return result;
    }

    public List<StudentsCourse> findCoursesByStudent(int id) {
            List<StudentsCourse> result = studentsCourseRepository.findByStudentId(id);
            logger.debug("find courses: {}", result);
            return result;
    }

    public List<StudentsCourse> findCoursesByCourseId(int id) {
            List<StudentsCourse> result = studentsCourseRepository.findByCourseId(id);
            logger.debug("find courses: {}", result);
            return result;
    }

    public void updateGrade(int grade, StudentsCourse studentsCourse) {
        studentsCourse.setGrade(grade);
        studentsCourseRepository.save(studentsCourse);
        logger.debug("setting new garde: {}", grade);
    }
    @Transactional(rollbackFor = { SQLException.class })
    public void addCourseToStudent(User student, int courseId) {
        StudentsCourse course = new StudentsCourse();
        course.setStudentId(student.getId());
        course.setCourseId(courseId);
        java.util.Date date = new Date();
        java.sql.Date dateSql = new java.sql.Date(date.getYear(), date.getMonth(), date.getDate());
        course.setRegistrationDate(dateSql);
        studentsCourseRepository.save(course);
        List<Subtopic> subtopicList = subtopicManager.findSubtopicsByCourse(course.getCourseId());
        for (Subtopic s : subtopicList) {
            StudentsSubtopic ss = new StudentsSubtopic(student.getId(), s.getSubtopicId());
            studentsSubtopicRepository.save(ss);
        }
        logger.debug("User: {} Joined course: {}", student, course);
    }

    public Map<Integer, Date> getCoursesRegistrationDate(List<Course> courses, User student) {
        Map<Integer, Date> result = new HashMap<>();
        for (Course course : courses) {
            result.put(course.getId(), studentsCourseRepository.findByCourseIdAndStudentId(course.getId(), student.getId()).get().getRegistrationDate());
        }
        return result;
    }

    public Map<Integer, Integer> getCoursesGrade(List<Course> courses, User student) {
        Map<Integer, Integer> result = new HashMap<>();
        for (Course course : courses) {
            result.put(course.getId(), studentsCourseRepository.findByCourseIdAndStudentId(course.getId(), student.getId()).get().getGrade());
        }
        return result;
    }


}
