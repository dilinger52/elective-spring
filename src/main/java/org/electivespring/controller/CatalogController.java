package org.electivespring.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.electivespring.database.entity.Course;
import org.electivespring.database.entity.Subtopic;
import org.electivespring.database.entity.User;
import org.electivespring.logic.*;
import org.electivespring.utils.Pagination;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Controller
public class CatalogController {

    @Autowired
    private CourseManager courseManager;
    @Autowired
    private UserManager userManager;
    @Autowired
    private StudentsCourseManager studentsCourseManager;
    @Autowired
    private StudentsSubtopicManager studentsSubtopicManager;
    @Autowired
    private SubtopicManager subtopicManager;

    @RequestMapping("/catalog")
    public String catalog(HttpSession session, HttpServletRequest req) {
        User user = (User) session.getAttribute("user");

        List<String> topics;
        List<User> teachers;
        List<Course> courses;
        Map<Integer, Integer> coursesStudents;
        Map<Integer, String> coursesTeacher;
        try {
            topics = courseManager.findAllTopics();
            teachers = userManager.findAllTeachers();

            if (user.getRoleId() == 2) {
                courses = courseManager.findCoursesByTeacher(user.getId());
            } else {
                courses = courseManager.findAllCourses();
                if (user.getRoleId() == 3) {
                    courses = courseManager.getAvailableCourses(user, courses);
                }
            }

            coursesStudents = courseManager.getCoursesStudents(courses);
            coursesTeacher = courseManager.getCoursesTeacher(courses);

        } catch (Exception e) {
            e.printStackTrace();
            req.setAttribute("message", e.getMessage());
            return "error";
        }

        Map<Integer, List<Course>> pages = Pagination.pageConstructor(courses);

        session.setAttribute("topics", topics);
        session.setAttribute("teachers", teachers);
        session.setAttribute("courses", courses);
        session.setAttribute("pages", pages);
        session.setAttribute("coursesStudents", coursesStudents);
        session.setAttribute("coursesTeacher", coursesTeacher);
        session.setAttribute("pageKey", 0);
        session.setAttribute("path", "catalog_page");
        return "catalog_page";
    }

    @RequestMapping("/personal_courses")
    public String personalCourses(HttpSession session, HttpServletRequest req) {
        User user = (User) session.getAttribute("user");

        List<String> topics;
        List<User> teachers;
        List<Course> courses;
        Map<Integer, String> coursesTeacher;
        Map<Integer, Date> coursesRegistrationDate;
        Map<Integer, Integer> studentsSubtopicNum;
        Map<Integer, Integer> finishedSubtopicsNum;
        Map<Integer, Integer> coursesGrade;
        try {
            topics = courseManager.findAllTopics();
            teachers = userManager.findAllTeachers();
            courses = courseManager.findCoursesByStudent(user.getId());
            coursesTeacher = courseManager.getCoursesTeacher(courses);
            coursesRegistrationDate = studentsCourseManager.getCoursesRegistrationDate(courses, user);
            studentsSubtopicNum = subtopicManager.getStudentsSubtopicsNum(courses);
            finishedSubtopicsNum = studentsSubtopicManager.getFinishedSubtopicsNum(courses, user);
            coursesGrade = studentsCourseManager.getCoursesGrade(courses, user);

        } catch (Exception e) {
            e.printStackTrace();
            req.setAttribute("message", e.getMessage());
            return "error";
        }

        Map<Integer, List<Course>> pages = Pagination.pageConstructor(courses);

        session.setAttribute("topics", topics);
        session.setAttribute("teachers", teachers);
        session.setAttribute("courses", courses);
        session.setAttribute("coursesTeacher", coursesTeacher);
        session.setAttribute("coursesRegistrationDate", coursesRegistrationDate);
        session.setAttribute("finishedSubtopicsNum", finishedSubtopicsNum);
        session.setAttribute("studentsSubtopicNum", studentsSubtopicNum);
        session.setAttribute("coursesGrade", coursesGrade);
        session.setAttribute("pages", pages);
        session.setAttribute("pageKey", 0);
        session.setAttribute("path", "personal_courses");
        return "catalog_page";
    }

    @PostMapping("/join_course")
    public String joinCourse(HttpSession session, @RequestParam String courseId, HttpServletRequest req) {
        User student = (User) session.getAttribute("user");

        try {
            studentsCourseManager.addCourseToStudent(student, Integer.parseInt(courseId));
        } catch (Exception e) {
            e.printStackTrace();
            req.setAttribute("message", e.getMessage());
            return "error";
        }
        return "redirect:/personal_courses";
    }


}
