package org.electivespring.database.dao;

import org.electivespring.database.entity.Subtopic;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface SubtopicRepository extends CrudRepository<Subtopic, Integer> {
    @Override
    <S extends Subtopic> S save(S entity);

    @Override
    Optional<Subtopic> findById(Integer integer);

    @Override
    void delete(Subtopic entity);
    @Modifying
    @Query(value = "select * from course_subtopics where course_id=?1", nativeQuery = true)
    List<Subtopic> findByCourse(int courseId);
}
