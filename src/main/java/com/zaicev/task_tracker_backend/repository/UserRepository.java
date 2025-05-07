package com.zaicev.task_tracker_backend.repository;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.zaicev.task_tracker_backend.models.User;


@Repository
public interface UserRepository extends CrudRepository<User, Long>{
	Optional<User> findByEmail(String email);
	
	boolean existsByEmail(String email);
}
