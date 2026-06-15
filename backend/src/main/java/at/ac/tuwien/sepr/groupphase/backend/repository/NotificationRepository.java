package at.ac.tuwien.sepr.groupphase.backend.repository;

import at.ac.tuwien.sepr.groupphase.backend.entity.Notification;
import at.ac.tuwien.sepr.groupphase.backend.entity.NotificationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findAllByRecipientIdOrderByCreatedAtDesc(Long recipientId);

    long countByRecipientIdAndIsReadFalse(Long recipientId);

    boolean existsByRecipientIdAndTypeAndReferenceIdAndIsReadFalse(
        Long recipientId, NotificationType type, Long referenceId);
}
