package com.synxo.repository;

import com.synxo.domain.model.UserNotification;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserNotificationRepository extends JpaRepository<UserNotification, Long> {

	List<UserNotification> findByRecipientUserIdAndDismissedAtIsNullOrderByCreatedAtAscIdAsc(Long recipientUserId);

	Optional<UserNotification> findByIdAndRecipientUserId(Long id, Long recipientUserId);
}
