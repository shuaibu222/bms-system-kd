package com.shuaibu.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.shuaibu.dto.UserDto;
import com.shuaibu.model.UserModel;

@Repository
public interface UserRepository extends JpaRepository<UserModel, Long> {
    UserModel findByUsername(String username);
    List<UserModel> findManyByUsername(String username);

    UserModel findUserModelByUsernameAndPassword(String username, String password);

    UserModel findByUsernameAndIsActive(String adminUsername, String string);
    List<UserModel> findManyByUsernameAndIsActive(String adminUsername, String string);
    boolean existsByUsernameAndIdNot(String username, Long id);
}

