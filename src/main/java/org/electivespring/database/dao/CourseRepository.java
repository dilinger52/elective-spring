package org.electivespring.database.dao;

import org.electivespring.database.entity.Course;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.lang.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public interface CourseRepository extends CrudRepository<Course, Integer> {
    @Override
    <S extends Course> S save(S entity);

    @Override
    Optional<Course> findById(Integer integer);

    @Override
    void deleteById(Integer integer);

    @Modifying
    @Query(value = "SELECT * FROM course WHERE name LIKE ?1", nativeQuery = true)
    List<Course> findByName(String pattern);

    @Override
    Iterable<Course> findAll();
    @Query(value = "select topic from course", nativeQuery = true)
    List<String> findAllTopics();

    List<Course> findByTeacherId(int teacher_id);
}
