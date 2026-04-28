package com.synxo.repository;

import com.synxo.domain.model.User;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

	Optional<User> findByEmail(String email);

	List<User> findByIdIn(Collection<Long> ids);

	boolean existsByEmail(String email);
}
