package org.electivespring.database.dao;

import org.electivespring.database.entity.StudentsCourse;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface StudentsCourseRepository extends CrudRepository<StudentsCourse, Integer> {
    @Override
    <S extends StudentsCourse> S save(S entity);
    Optional<StudentsCourse> findByCourseIdAndStudentId(int courseId, int studentId);
    @Override
    void delete(StudentsCourse entity);
    List<StudentsCourse> findByStudentId(int studentId);
    List<StudentsCourse> findByCourseId(int courseId);
}
