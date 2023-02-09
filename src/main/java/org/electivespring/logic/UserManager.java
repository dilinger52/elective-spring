package org.electivespring.logic;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.electivespring.database.dao.UserRepository;
import org.electivespring.database.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Service
public class UserManager {

    private static final Logger logger = LogManager.getLogger(UserManager.class);
    @Autowired
    private final UserRepository userRepository;
    public UserManager(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User findUserByEmail(String email) {
        Optional<User> optUser = userRepository.findByEmail(email);
        if (optUser.isPresent()) {
            User user = optUser.get();
            logger.debug("find user: {}", user);
            return user;
        }
        return null;
    }

    public User findUserById(int id) {
        Optional<User> optUser = userRepository.findById(id);
        if (optUser.isPresent()) {
            User user = optUser.get();
            logger.debug("find user: {}", user);
            return user;
        }
        return null;
    }

    public void insertUser(User user) {
        userRepository.save(user);
        logger.debug("User created successfully");
    }

    public List<User> findAllTeachers() {
        List<User> teachers = userRepository.findAllTeachers();
        logger.debug("find teachers: {}", teachers);
        return teachers;

    }

    public List<User> findAllStudents() {
        List<User> students = userRepository.findAllStudents();
        logger.debug("find students: {}", students);
        return students;
    }

    public void blockStudent(int studentId) throws SQLException {
        User student = findUserById(studentId);
        student.setBlocked(!student.isBlocked().equals("true"));
        userRepository.save(student);
        logger.debug("Student blocked");

    }


}
