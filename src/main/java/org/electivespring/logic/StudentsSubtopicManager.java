package org.electivespring.logic;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.electivespring.database.dao.StudentsSubtopicRepository;
import org.electivespring.database.entity.Course;
import org.electivespring.database.entity.StudentsSubtopic;
import org.electivespring.database.entity.Subtopic;
import org.electivespring.database.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

import static org.electivespring.database.entity.StudentsSubtopic.completion.COMPLETED;
@Service
public class StudentsSubtopicManager {
    private static final Logger logger = LogManager.getLogger(StudentsSubtopicManager.class);

    @Autowired
    private final StudentsSubtopicRepository studentsSubtopicRepository;
    @Autowired
    private SubtopicManager subtopicManager;

    public StudentsSubtopicManager(StudentsSubtopicRepository studentsSubtopicRepository) {
        this.studentsSubtopicRepository = studentsSubtopicRepository;
    }

    public Map<Integer, String> getSubtopicCompletion(User student, int courseId) throws SQLException {
        List<Subtopic> subtopics = subtopicManager.findSubtopicsByCourse(courseId);
        Map<Integer, String> result = new HashMap<>();
        int i = 0;
        for (Subtopic subtopic : subtopics) {
            result.put(i, getCompletion(subtopic.getSubtopicId(), student.getId()));
            i++;
        }
        return result;
    }

    private String getCompletion(int subtopicId, int studentId) throws SQLException {
        return findStudentsSubtopic(subtopicId, studentId).getCompletion();
    }
    @Transactional(rollbackFor = { SQLException.class })
    public Map<Integer, String> changeCompletion(int key, User student, Map<Integer, Subtopic> pages, Course course) throws SQLException {
        Map<Integer, String> subtopicCompletion = new HashMap<>();
        Subtopic subtopic = pages.get(key);
        Optional<StudentsSubtopic> optStudentsSubtopic = studentsSubtopicRepository.findBySubtopicIdAndStudentId(subtopic.getSubtopicId(), student.getId());
        if (optStudentsSubtopic.isEmpty()) {
            throw new SQLException();
        }
        StudentsSubtopic studentsSubtopic = optStudentsSubtopic.get();
        changeCompletion(studentsSubtopicRepository, studentsSubtopic);

        List<Subtopic> subtopics = subtopicManager.findSubtopicsByCourse(course.getId());

        updateCompletion(student, pages, subtopicCompletion, studentsSubtopicRepository, subtopics);
        return subtopicCompletion;
    }

    private void changeCompletion(StudentsSubtopicRepository studentsSubtopicRepository, StudentsSubtopic studentsSubtopic) {
        if (!studentsSubtopic.getCompletion().equals(String.valueOf(COMPLETED))) {
            studentsSubtopic.setCompletion(String.valueOf(COMPLETED));
            studentsSubtopicRepository.save(studentsSubtopic);
        }
    }

    private void updateCompletion(User student, Map<Integer, Subtopic> pages, Map<Integer, String> subtopicCompletion,
                                  StudentsSubtopicRepository studentsSubtopicRepository, List<Subtopic> subtopics) {
        int i = 0;
        for (Subtopic s : subtopics) {
            pages.put(i, s);
            subtopicCompletion.put(i, studentsSubtopicRepository.findBySubtopicIdAndStudentId(s.getSubtopicId(), student.getId()).get().getCompletion());
            i++;
        }
    }

    public List<StudentsSubtopic> getFinishedSubtopics(int studentId, int courseId) throws SQLException {
        return findAllStudentsSubtopic(studentId, courseId).stream()
                .filter(s -> s.getCompletion().equals(COMPLETED.toString()))
                .collect(Collectors.toList());
    }

    public Map<Integer, Integer> getFinishedSubtopicsNum(List<Course> courses, User student) throws SQLException {
        Map<Integer, Integer> result = new HashMap<>();
        int i = 0;
        for (Course course : courses) {
            result.put(i++, getFinishedSubtopics(student.getId(), course.getId()).size());
        }
        return result;
    }

    public StudentsSubtopic findStudentsSubtopic(int subtopicId, int studentId) throws SQLException {
            Optional<StudentsSubtopic> optSubtopic = studentsSubtopicRepository.findBySubtopicIdAndStudentId(subtopicId, studentId);
            if (optSubtopic.isPresent()) {
                StudentsSubtopic subtopic = optSubtopic.get();
                logger.debug("subtopic properties: {}", subtopic);
                return subtopic;
            }
            throw new SQLException();
    }

    public List<StudentsSubtopic> findAllStudentsSubtopic(int studentId, int courseId) throws SQLException {
        List<StudentsSubtopic> studentsSubtopics = new ArrayList<>();
        List<Subtopic> subtopics = subtopicManager.findSubtopicsByCourse(courseId);
        for (Subtopic subtopic : subtopics) {
            studentsSubtopics.add(findStudentsSubtopic(subtopic.getSubtopicId(), studentId));
        }
        return studentsSubtopics;
    }
}
