package org.electivespring.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.electivespring.database.entity.Course;
import org.electivespring.database.entity.Subtopic;
import org.electivespring.database.entity.User;
import org.electivespring.logic.CourseManager;
import org.electivespring.logic.StudentsSubtopicManager;
import org.electivespring.utils.Filter;
import org.electivespring.utils.Sorting;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.electivespring.utils.Pagination.getNewPageKey;
import static org.electivespring.utils.Pagination.pageConstructor;

@Controller
public class UtilController {

    @Autowired
    private CourseManager courseManager;

    @Autowired
    private StudentsSubtopicManager studentsSubtopicManager;
    @Autowired
    private Filter filter;

    @PostMapping("/log_out")
    public String logOut(HttpSession session) {
        session.removeAttribute("user");
        return "redirect:/log_in";
    }

    @RequestMapping("/filter")
    public String filter(@RequestParam(required = false) List<String> topic, @RequestParam(required = false) List<String> teacher,
                         @RequestParam(required = false) List<String> completion, @RequestParam HashMap<String, Object> params,
                         HttpSession session, HttpServletRequest req) {
        String sortingPattern = (String) params.get("sorting_pattern");
        String pattern = (String) params.get("pattern");
        String path = (String) session.getAttribute("path");
        User user = (User) session.getAttribute("user");
        List<Course> courses = (List<Course>) session.getAttribute("courses");
        courses = filter.filterName(pattern, courses);
        courses = filter.filterTopic(topic, courses);
        courses = filter.filterTeachers(teacher, courses);
        try {
            courses = filter.filterCompletions(completion, courses, user);
        } catch (Exception e) {
            e.printStackTrace();
            req.setAttribute("message", e.getMessage());
            return "error";
        }
        courses = Sorting.sort(session, courses, sortingPattern);

        session.setAttribute("pages", pageConstructor(courses));
        session.setAttribute("pageKey", 0);
        if (path.equals("personal_courses")) {
            return "catalog_page";
        }
        return path;
    }

    @RequestMapping("/pagination")
    public String changePage(@RequestParam int new_page_key, HttpSession session, HttpServletRequest req) {
        Map<Integer, Subtopic> pages = (Map<Integer, Subtopic>) session.getAttribute("pages");
        int key = (int) session.getAttribute("pageKey");
        int newPageKey = getNewPageKey(new_page_key, pages, key);
        String path = (String) session.getAttribute("path");
        Course course = (Course) session.getAttribute("course");

        if (path.equals("course_content")) {
            Map<Integer, String> subtopicCompletion;

            User student = (User) session.getAttribute("user");

            try {
                subtopicCompletion = studentsSubtopicManager.changeCompletion(key, student, pages, course);
            } catch (Exception e) {
                e.printStackTrace();
                req.setAttribute("message", e.getMessage());
                return "error";
            }

            session.setAttribute("subtopicCompletion", subtopicCompletion);
        }

        session.setAttribute("pageKey", newPageKey);
        session.setAttribute("path", path);
        if (path.equals("personal_courses")) {
            return "catalog_page";
        }
        return path;
    }

}
