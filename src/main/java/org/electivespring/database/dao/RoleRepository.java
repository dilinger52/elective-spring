package org.electivespring.database.dao;

import org.electivespring.database.entity.Role;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;


public interface RoleRepository extends CrudRepository<Role, Integer> {
    @Override
    <S extends Role> S save(S entity);

    @Override
    Optional<Role> findById(Integer integer);

    @Override
    void delete(Role entity);
}
