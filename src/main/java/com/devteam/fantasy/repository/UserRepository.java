package com.devteam.fantasy.repository;


import com.devteam.fantasy.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

	@Query(value = "SELECT u.* FROM users u WHERE u.dtype = 'User' AND u.user_state = 1 ", nativeQuery = true)
	List<User> getAllAdmin();
	
	Optional<User> findByUsername(String username);

    Boolean existsByUsername(String username);

    List<User> findByOrderByIdAsc();

    User getByUsername(String username);

    User getById(Long id);

	User findFirstBy();

}