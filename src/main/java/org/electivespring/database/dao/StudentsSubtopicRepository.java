package org.electivespring.database.dao;

import org.electivespring.database.entity.StudentsSubtopic;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface StudentsSubtopicRepository extends CrudRepository<StudentsSubtopic, Integer> {

    @Override
    <S extends StudentsSubtopic> S save(S entity);
    Optional<StudentsSubtopic> findBySubtopicIdAndStudentId(int subtopicId, int studentId);
    @Override
    void delete(StudentsSubtopic entity);
}
