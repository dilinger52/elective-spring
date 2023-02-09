package org.electivespring.logic;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.electivespring.database.dao.StudentsCourseRepository;
import org.electivespring.database.dao.StudentsSubtopicRepository;
import org.electivespring.database.dao.SubtopicRepository;
import org.electivespring.database.entity.Course;
import org.electivespring.database.entity.StudentsCourse;
import org.electivespring.database.entity.StudentsSubtopic;
import org.electivespring.database.entity.Subtopic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class SubtopicManager {
    private static final Logger logger = LogManager.getLogger(SubtopicManager.class);

    @Autowired
    private final SubtopicRepository subtopicRepository;
    @Autowired
    private StudentsCourseRepository studentsCourseRepository;
    @Autowired
    private StudentsSubtopicRepository studentsSubtopicRepository;
    public SubtopicManager(SubtopicRepository subtopicRepository) {
        this.subtopicRepository = subtopicRepository;
    }
    @Transactional(rollbackFor = { SQLException.class })
    public void addSubtopicToCourse(int courseId) {
        Subtopic subtopic = new Subtopic(courseId, "Default name");
        subtopicRepository.save(subtopic);
        List<StudentsCourse> courses = studentsCourseRepository.findByCourseId(courseId);
        for (StudentsCourse studentsCourse : courses) {
            StudentsSubtopic studentsSubtopic = new StudentsSubtopic(studentsCourse.getStudentId(), subtopic.getSubtopicId());
            studentsSubtopicRepository.save(studentsSubtopic);
        }
        logger.debug("Created subtopic: {}; for course id: {}", subtopic, courseId);
    }

    public List<Subtopic> findSubtopicsByCourse(int courseId) {
        List<Subtopic> subtopics = subtopicRepository.findByCourse(courseId);
        logger.debug("subtopics properties: {}", subtopics);
        return subtopics;
    }

    public Map<Integer, Integer> getStudentsSubtopicsNum(List<Course> courses) {
        Map<Integer, Integer> result = new HashMap<>();
        int i = 0;
        for (Course course : courses) {
            result.put(i++, findSubtopicsByCourse(course.getId()).size());
        }
        return result;
    }


    public void updateSubtopic(int id, String name, String content) throws SQLException {
        Optional<Subtopic> optSubtopic = subtopicRepository.findById(id);
        if (optSubtopic.isEmpty()) {
            throw new SQLException();
        }
        Subtopic subtopic = optSubtopic.get();
        subtopic.setSubtopicName(name);
        subtopic.setSubtopicContent(content);
        subtopicRepository.save(subtopic);
        logger.debug("subtopic properties: {}", subtopic);
    }

    public void deleteSubtopic(int id) throws SQLException {
        Optional<Subtopic> optSubtopic = subtopicRepository.findById(id);
        if (optSubtopic.isPresent()) {
            subtopicRepository.delete(optSubtopic.get());
            logger.debug("Successfully deleting");
            return;
        }
        throw new SQLException();
    }


}
