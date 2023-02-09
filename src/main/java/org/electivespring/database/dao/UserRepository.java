package org.electivespring.database.dao;

import org.electivespring.database.entity.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
@Repository
public interface UserRepository extends CrudRepository<User, Integer> {

    @Override
    <S extends User> S save(S entity);
    @Override
    Optional<User> findById(Integer integer);
    Optional<User> findByLastName(String lastName);
    Optional<User> findByEmail(String email);
    @Override
    void delete(User entity);
    @Query(value = "select * from user where role_id=3", nativeQuery = true)
    List<User> findAllStudents();
    @Query(value = "select * from user where role_id=2", nativeQuery = true)
    List<User> findAllTeachers();
}
