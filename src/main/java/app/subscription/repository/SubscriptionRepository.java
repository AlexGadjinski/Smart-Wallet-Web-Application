package app.subscription.repository;

import app.subscription.model.Subscription;
import app.subscription.model.SubscriptionStatus;
import app.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, UUID> {
    Optional<Subscription> findByOwnerIdAndStatus(UUID ownerId, SubscriptionStatus status);

    List<Subscription> findAllByStatusAndCompletedOnLessThanEqual(SubscriptionStatus status, LocalDateTime now);
}
