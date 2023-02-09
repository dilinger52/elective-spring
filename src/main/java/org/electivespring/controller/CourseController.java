package org.electivespring.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.electivespring.database.entity.*;
import org.electivespring.logic.*;
import org.electivespring.utils.Pagination;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.electivespring.database.entity.StudentsSubtopic.completion.COMPLETED;


@Controller
public class CourseController {

    private static final String SELECTED = "selected";
    private static final String MESSAGE = "message";
    private static final String ERROR_PAGE = "error.html";
    private static final String COURSE_ID = "courseId";

    @Autowired
    private CourseManager courseManager;
    @Autowired
    private StudentsSubtopicManager studentsSubtopicManager;
    @Autowired
    private Pagination pagination;
    @Autowired
    private UserManager userManager;
    @Autowired
    private StudentsCourseManager studentsCourseManager;
    @Autowired
    private SubtopicManager subtopicManager;

    @RequestMapping("/course_content")
    public String courseContent(HttpSession session, HttpServletRequest req) {
        User student = (User) session.getAttribute("user");
        int courseId = Integer.parseInt(req.getParameter("courseId"));

        Map<Integer, Subtopic> pages;
        Course course;
        Map<Integer, String> subtopicCompletion;
        try {
            course = courseManager.findCourseById(courseId);
            pages = pagination.getSubtopicPages(courseId);
            subtopicCompletion = studentsSubtopicManager.getSubtopicCompletion(student, courseId);
        } catch (Exception e) {
            e.printStackTrace();
            req.setAttribute("message", e.getMessage());
            return "error";
        }

        session.setAttribute("course", course);
        session.setAttribute("pages", pages);
        session.setAttribute("subtopicCompletion", subtopicCompletion);
        session.setAttribute("pageKey", 0);
        session.setAttribute("path", "course_content");
        return "course_content";
    }

    @GetMapping("/create_course")
    public String createCourse(HttpSession session, HttpServletRequest req) {
        session.removeAttribute("course");
        session.removeAttribute(SELECTED);
        session.removeAttribute("selectedTopic");

        List<User> teachers;
        List<String> topics;
        try {
            teachers = userManager.findAllTeachers();
            topics = courseManager.findAllTopics();
        } catch (Exception e) {
            e.printStackTrace();
            req.setAttribute("message", e.getMessage());
            return "error";
        }

        if (req.getParameter(COURSE_ID) != null && !"0".equals(req.getParameter(COURSE_ID))) {
            int courseId = Integer.parseInt(req.getParameter(COURSE_ID));
            System.out.println(courseId);
            Course course;
            try {
                course = courseManager.findCourseById(courseId);
            } catch (Exception e) {
                e.printStackTrace();
                req.setAttribute("message", e.getMessage());
                return "error";
            }
            session.setAttribute("course", course);
        }

        session.setAttribute("teachers", teachers);
        session.setAttribute("topics", topics);
        return "create_course";
    }

    @PostMapping("/create_course")
    public String createCourse(HttpServletRequest req) {
        String name = req.getParameter("name");
        String topic = getTopic(req.getParameterValues("topic"));
        String description = req.getParameter("description");
        long duration = Long.parseLong(req.getParameter("duration"));
        int teacherId = Integer.parseInt(req.getParameter("teacher"));

        try {
            if (req.getParameter(COURSE_ID) != null) {
                int courseId = Integer.parseInt(req.getParameter(COURSE_ID));
                courseManager.updateCourse(courseId, name, topic, description, teacherId, duration);
            } else {
                courseManager.insertCourse(new Course(teacherId, name, topic, description, duration));
            }
        } catch (Exception e) {
            e.printStackTrace();
            req.setAttribute("message", e.getMessage());
            return "error";
        }
        return "redirect:/catalog";
    }

    @PostMapping("/delete_course")
    public String deleteCourse(HttpServletRequest req){
        int courseId = Integer.parseInt(req.getParameter("courseId"));
        courseManager.deleteCourse(courseId);
        return "redirect:/catalog";
    }

    private String getTopic(String[] topics) {
        String topic;
        if (topics[1].length() > 0) {
            topic = topics[1];
        } else {
            topic = topics[0];
        }
        return topic;
    }

    @GetMapping("/view_course")
    public String viewCourseStatistic(HttpSession session, HttpServletRequest req) {
        int courseId = getCourseId(req, session);

        List<User> students = new ArrayList<>();
        List<StudentsCourse> coursesStudents;
        Course course;
        Map<Integer, Map<String, Boolean>> studentsGrade = new HashMap<>();
        Map<Integer, Integer> studentsCoursesNum = new HashMap<>();
        Map<Integer, Integer> finishedCoursesNum = new HashMap<>();

        try {
            course = courseManager.findCourseById(courseId);
            coursesStudents = studentsCourseManager.findCoursesByCourseId(courseId);

            for (StudentsCourse coursesStudent : coursesStudents) {
                students.add(userManager.findUserById(coursesStudent.getStudentId()));
                List<Subtopic> subtopics = subtopicManager.findSubtopicsByCourse(course.getId());

                List<StudentsSubtopic> studentsSubtopics = new ArrayList<>();
                for (Subtopic subtopic : subtopics) {
                    studentsSubtopics.add(studentsSubtopicManager.findStudentsSubtopic(subtopic.getSubtopicId(), coursesStudent.getStudentId()));
                }

                List<StudentsSubtopic> finishedSubtopics = studentsSubtopics.stream()
                        .filter(s -> s.getCompletion().equals(COMPLETED.toString()))
                        .toList();

                finishedCoursesNum.put(coursesStudent.getStudentId(), finishedSubtopics.size());
                studentsCoursesNum.put(coursesStudent.getStudentId(), studentsSubtopics.size());

                Map<String, Boolean> checked = new HashMap<>();
                for (int i = 1; i <= 5; i++) {
                    if (coursesStudent.getGrade() != null && coursesStudent.getGrade() == i) {
                        checked.put("i" + i, true);
                    } else {
                        checked.put("i" + i, false);
                    }
                }
                studentsGrade.put(coursesStudent.getStudentId(), checked);
            }
        } catch (Exception e) {
            e.printStackTrace();
            req.setAttribute("message", e.getMessage());
            return "error";
        }

        session.setAttribute("course", course);
        session.setAttribute("students", students);
        session.setAttribute("finishedCoursesNum", finishedCoursesNum);
        session.setAttribute("studentsCoursesNum", studentsCoursesNum);
        session.setAttribute("studentsGrade", studentsGrade);

        return "teacher_course_page";
    }

    private int getCourseId(HttpServletRequest req, HttpSession session) {
        int courseId;
        if (req.getParameter("courseId") == null) {
            courseId = ((Course) session.getAttribute("course")).getId();
        } else {
            courseId = Integer.parseInt(req.getParameter("courseId"));
        }
        return courseId;
    }

    @PostMapping("/rate")
    public String rateStudent(HttpServletRequest req) {
        int studentId = Integer.parseInt(req.getParameter("student"));

        int grade;
        if (req.getParameter("grade") != null) {
            grade = Integer.parseInt(req.getParameter("grade"));
        } else {
            req.setAttribute(MESSAGE, "ChooseValidGrade");
            return "teacher_course_page";
        }

        HttpSession session = req.getSession();
        Course course = (Course) session.getAttribute("course");

        List<StudentsSubtopic> studentsSubtopics;
        List<StudentsSubtopic> finishedSubtopics;
        try {
            studentsSubtopics = studentsSubtopicManager.findAllStudentsSubtopic(studentId, course.getId());
            finishedSubtopics = studentsSubtopicManager.getFinishedSubtopics(studentId, course.getId());
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException();
        }

        if (finishedSubtopics.size() != studentsSubtopics.size()) {
            req.setAttribute(MESSAGE, "ThisStudentDidNotCompleteCourseYet");
            return "teacher_course_page";
        }

        try {
            studentsCourseManager.updateGrade(grade, studentsCourseManager.findStudentsCourse(course.getId(), studentId));
        } catch (Exception e) {
            req.setAttribute(MESSAGE, e.getMessage());
            return "teacher_course_page";
        }
        return "redirect:/view_course";
    }

    @GetMapping("/content_redactor")
    public String showContentRedactor(HttpSession session, HttpServletRequest req) {
        int courseId = getCourseId(req, session);

        Map<Integer, Subtopic> pages;
        Course course;
        try {
            course = courseManager.findCourseById(courseId);
            pages = pagination.getSubtopicPages(courseId);
        } catch (Exception e) {
            e.printStackTrace();
            req.setAttribute("message", e.getMessage());
            return "error";
        }

        session.setAttribute("course", course);
        session.setAttribute("pages", pages);
        setPageKey(session, pages);
        session.setAttribute("path", "course_content_redactor");

        return "course_content_redactor";
    }

    private void setPageKey(HttpSession session, Map<Integer, Subtopic> pages) {
        if (session.getAttribute("path").equals("new_subtopic")) {
            session.setAttribute("pageKey", pages.size() - 1);
            return;
        }
        if (session.getAttribute("pageKey") == null) {
            session.setAttribute("pageKey", 0);
        }

    }

    @PostMapping("/content_redactor")
    public String saveSubtopic(HttpServletRequest req) {
        int subtopicId = Integer.parseInt(req.getParameter("subtopicId"));
        String subtopicName = req.getParameter("subtopicName");
        String subtopicContent = req.getParameter("subtopicContent");

        try {
            subtopicManager.updateSubtopic(subtopicId, subtopicName, subtopicContent);
        } catch (Exception e) {
            e.printStackTrace();
            req.setAttribute("message", e.getMessage());
            return "error";
        }

        return "redirect:/content_redactor";
    }

    @PostMapping("/delete_subtopic")
    public String deleteSubtopic(HttpServletRequest req, HttpSession session) {
        int subtopicId = Integer.parseInt(req.getParameter("subtopicId"));
        try {
            subtopicManager.deleteSubtopic(subtopicId);
        } catch (Exception e) {
            e.printStackTrace();
            req.setAttribute("message", e.getMessage());
            return "error";
        }

        session.setAttribute("pageKey", 0);

        return "redirect:/content_redactor";
    }

    @PostMapping("/new_subtopic")
    public String newSubtopic(HttpServletRequest req) {
        int courseId = Integer.parseInt(req.getParameter("courseId"));
        HttpSession session = req.getSession();

        try {
            subtopicManager.addSubtopicToCourse(courseId);
        } catch (Exception e) {
            req.setAttribute("message", e.getMessage());
            return "error";
        }

        session.setAttribute("path", "new_subtopic");

        return "redirect:/content_redactor";
    }
}
