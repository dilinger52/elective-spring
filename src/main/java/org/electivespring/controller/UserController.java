package org.electivespring.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.electivespring.database.entity.StudentsSubtopic;
import org.electivespring.database.entity.Subtopic;
import org.electivespring.database.entity.User;
import org.electivespring.database.entity.StudentsCourse;
import org.electivespring.logic.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.mail.MessagingException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.electivespring.database.entity.StudentsSubtopic.completion.COMPLETED;

@Controller
public class UserController {

    private static final String PASSWORD = "password";
    private static final String CONFPASS = "confpass";
    private static final String EMAIL = "email";
    private static final String FIRST_NAME = "first_name";
    private static final String LAST_NAME = "last_name";
    @Autowired
    private UserManager userManager;
    @Autowired
    private StudentsCourseManager studentsCourseManager;
    @Autowired
    private SubtopicManager subtopicManager;
    @Autowired
    private StudentsSubtopicManager studentsSubtopicManager;

    @RequestMapping("/log_in")
    public String authorisation() {
        return "authorisation";
    }

    @PostMapping("/check_user")
    public String checkUser(Model model, HttpSession session, @RequestParam HashMap<String, Object> params) throws IOException {
        String email = (String) params.get("email");
        String password = (String) params.get("password");
        User user;
        user = userManager.findUserByEmail(email);
        if (user == null) {
            model.addAttribute("emailmes", "InvalidEmail");
            return "authorisation";
        }
        if (!password.equals(user.getPassword())) {
            model.addAttribute("password", "InvalidPassword");
            return "authorisation";
        }
        session.setAttribute("user", user);
        return "redirect:/catalog";
    }

    @GetMapping("/registration")
    public String getRegisterPage(HttpSession session) {
        session.removeAttribute(EMAIL);
        session.removeAttribute(FIRST_NAME);
        session.removeAttribute(LAST_NAME);
        session.removeAttribute(PASSWORD);
        session.removeAttribute(CONFPASS);
        return "registration";
    }

    @PostMapping("/registration")
    public String registerUser(HttpSession session, HttpServletRequest req) {
        String firstName = req.getParameter(FIRST_NAME);
        String lastName = req.getParameter(LAST_NAME);
        String password = req.getParameter(PASSWORD);
        String confpass = req.getParameter(CONFPASS);
        String email = req.getParameter(EMAIL);

        session.removeAttribute(EMAIL);
        session.removeAttribute(FIRST_NAME);
        session.removeAttribute(LAST_NAME);
        session.removeAttribute(PASSWORD);
        session.removeAttribute(CONFPASS);

        if (!firstName.matches("[A-ZА-Я][a-zа-я]+")) {
            session.setAttribute(FIRST_NAME, "FirstNameMustStartsWithUppercase");
            return "registration";
        }

        if (!lastName.matches("[A-ZА-Я][a-zа-я]+")) {
            session.setAttribute("lastName", "LastNameMustStartsWithUppercase");
            return "registration";
        }

        if (password.length() < 8) {
            session.setAttribute(PASSWORD, "PasswordLengthMustBeAtLeast8Symbols");
            return "registration";
        }

        if (!email.matches("[a-z0-9]+@[a-z]+.[a-z]+")) {
            session.setAttribute(EMAIL, "EmailMustContain@And.");
            return "registration";
        }

        if (!confpass.equals(password)) {
            session.setAttribute(CONFPASS, "PasswordsDoesn'tTheSame");
            return "registration";
        }

        try {
            User user = userManager.findUserByEmail(email);
            if (user != null) {
                session.setAttribute(EMAIL, "UserWithThisEmailHasAlreadyRegistered");
                return "registration";
            }
        } catch (Exception e) {
            e.printStackTrace();
            req.setAttribute("message", e.getMessage());
            return "error";
        }

        try {
            MailManager.sendConfirmationLetter(firstName, lastName, password, email);
            session.setAttribute(EMAIL, email);
            return "confirm_email";
        } catch (MessagingException e) {
            e.printStackTrace();
            req.setAttribute("message", e.getMessage());
            return "error";
        }
    }

    @GetMapping("/students")
    public String showStudents(HttpSession session, HttpServletRequest req) {
        List<User> students;
        Map<Integer, List<StudentsCourse>> registeredCourses = new HashMap<>();
        Map<Integer, List<StudentsCourse>> startedCourses = new HashMap<>();
        Map<Integer, List<StudentsCourse>> finishedCourses = new HashMap<>();
        Map<Integer, Integer> studentsGrade = new HashMap<>();

        try {
            students = userManager.findAllStudents();
            for (User student : students) {
                List<StudentsCourse> studentsCourse = studentsCourseManager.findCoursesByStudent(student.getId());
                List<StudentsCourse> rc = new ArrayList<>();
                List<StudentsCourse> sc = new ArrayList<>();
                List<StudentsCourse> fc = new ArrayList<>();
                for (StudentsCourse course : studentsCourse) {
                    List<Subtopic> subtopics = subtopicManager.findSubtopicsByCourse(course.getCourseId());

                    List<StudentsSubtopic> studentsSubtopics = new ArrayList<>();
                    for (Subtopic subtopic : subtopics) {
                        studentsSubtopics.add(studentsSubtopicManager.findStudentsSubtopic(subtopic.getSubtopicId(), student.getId()));
                    }

                    List<StudentsSubtopic> finishedSubtopics = studentsSubtopics.stream()
                            .filter(s -> s.getCompletion().equals(COMPLETED.toString()))
                            .toList();

                    if (finishedSubtopics.size() == studentsSubtopics.size()) {
                        fc.add(course);
                    } else if (finishedSubtopics.isEmpty()) {
                        rc.add(course);
                    } else {
                        sc.add(course);
                    }
                }
                registeredCourses.put(student.getId(), rc);
                startedCourses.put(student.getId(), sc);
                finishedCourses.put(student.getId(), fc);

                int averageGrade = (int) (fc.stream().mapToInt(c -> (int) c.getGrade()).average()).orElse(-1);
                studentsGrade.put(student.getId(), averageGrade);
            }

        } catch (Exception e) {
            e.printStackTrace();
            req.setAttribute("message", e.getMessage());
            return "error";
        }

        session.setAttribute("students", students);
        session.setAttribute("registeredCourses", registeredCourses);
        session.setAttribute("startedCourses", startedCourses);
        session.setAttribute("finishedCourses", finishedCourses);
        session.setAttribute("studentsGrade", studentsGrade);

        return "admin_students_page";
    }

    @PostMapping("/students")
    public String blockStudent(HttpServletRequest req) {
        int studentId = Integer.parseInt(req.getParameter("student"));

        try {
            userManager.blockStudent(studentId);
        } catch (Exception e) {
            e.printStackTrace();
            req.setAttribute("message", e.getMessage());
            return "error";
        }
        return "redirect:/students";
    }

    @GetMapping("/create_teacher")
    public String createTeacher(HttpSession session) {
        session.removeAttribute(EMAIL);
        session.removeAttribute(FIRST_NAME);
        session.removeAttribute(LAST_NAME);
        session.removeAttribute(PASSWORD);
        session.removeAttribute(CONFPASS);
        return "create_teacher";
    }

    @PostMapping("/create_teacher")
    public String createTeacher(HttpSession session, HttpServletRequest req) {
        String firstName = req.getParameter(FIRST_NAME);
        String lastName = req.getParameter(LAST_NAME);
        String password = req.getParameter(PASSWORD);
        String confpass = req.getParameter(CONFPASS);
        String email = req.getParameter(EMAIL);

        session.removeAttribute(EMAIL);
        session.removeAttribute(FIRST_NAME);
        session.removeAttribute(LAST_NAME);
        session.removeAttribute(PASSWORD);
        session.removeAttribute(CONFPASS);

        if (!firstName.matches("[A-ZА-Я][a-zа-я]+")) {
            session.setAttribute(FIRST_NAME, "FirstNameMustStartsWithUppercase");
            return "create_teacher";
        }

        if (!lastName.matches("[A-ZА-Я][a-zа-я]+")) {
            session.setAttribute("lastName", "LastNameMustStartsWithUppercase");
            return "create_teacher";
        }

        if (password.length() < 8) {
            session.setAttribute(PASSWORD, "PasswordLengthMustBeAtLeast8Symbols");
            return "create_teacher";
        }

        if (!email.matches("[a-z0-9]+@[a-z]+.[a-z]+")) {
            session.setAttribute(EMAIL, "EmailMustContain@And.");
            return "create_teacher";
        }

        if (!confpass.equals(password)) {
            session.setAttribute(CONFPASS, "PasswordsDoesn'tTheSame");
            return "create_teacher";
        }

        try {
            User user = userManager.findUserByEmail(email);
            if (user != null) {
                session.setAttribute(EMAIL, "UserWithThisEmailHasAlreadyRegistered");
                return "create_teacher";
            }

            userManager.insertUser(new User(firstName, lastName, password, 2, email));
        } catch (Exception e) {
            e.printStackTrace();
            req.setAttribute("message", e.getMessage());
            return "error";
        }
        return "redirect:/create_course";
    }
}
