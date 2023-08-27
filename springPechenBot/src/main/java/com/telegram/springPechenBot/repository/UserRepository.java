package com.telegram.springPechenBot.repository;

import com.telegram.springPechenBot.model.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends CrudRepository<User, Long> {
}
