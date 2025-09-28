package app.subscription.service;

import app.exception.DomainException;
import app.subscription.model.Subscription;
import app.subscription.model.SubscriptionPeriod;
import app.subscription.model.SubscriptionStatus;
import app.subscription.model.SubscriptionType;
import app.subscription.repository.SubscriptionRepository;
import app.transaction.model.Transaction;
import app.transaction.model.TransactionStatus;
import app.user.model.User;
import app.wallet.service.WalletService;
import app.web.dto.UpgradeRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class SubscriptionService {
    private final SubscriptionRepository subscriptionRepository;
    private final WalletService walletService;

    public SubscriptionService(SubscriptionRepository subscriptionRepository, WalletService walletService) {
        this.subscriptionRepository = subscriptionRepository;
        this.walletService = walletService;
    }

    public Subscription createDefaultSubscription(User user) {
        Subscription subscription = subscriptionRepository.save(initializeDefaultSubscription(user));
        log.info("Successfully created new subscription with id [%s] and type [%s]".formatted(
                subscription.getId(), subscription.getType()
        ));
        return subscription;
    }

    private Subscription initializeDefaultSubscription(User user) {
        LocalDateTime now = LocalDateTime.now();

        return Subscription.builder()
                .owner(user)
                .status(SubscriptionStatus.ACTIVE)
                .period(SubscriptionPeriod.MONTHLY)
                .type(SubscriptionType.DEFAULT)
                .price(new BigDecimal("0.00"))
                .renewalAllowed(true)
                .createdOn(now)
                .completedOn(now.plusMonths(1))
                .build();
    }

    @Transactional
    public Transaction upgrade(User user, SubscriptionType subscriptionType, UpgradeRequest upgradeRequest) {

        Optional<Subscription> optionalSubscription = subscriptionRepository.findByOwnerIdAndStatus(user.getId(), SubscriptionStatus.ACTIVE);
        if (optionalSubscription.isEmpty()) {
            throw new DomainException("No active subscription has been found for user with id [%s]".formatted(user.getId()));
        }

        Subscription currentSubscription = optionalSubscription.get();

        SubscriptionPeriod subscriptionPeriod = upgradeRequest.getSubscriptionPeriod();
        BigDecimal subscriptionPrice = getSubscriptionPrice(subscriptionType, subscriptionPeriod);
        String chargeDescription = "Purchase of %s %s subscription".formatted(
                subscriptionPeriod.name().charAt(0) + subscriptionPeriod.name().substring(1).toLowerCase(),
                subscriptionType.name().charAt(0) + subscriptionType.name().substring(1).toLowerCase());

        Transaction charge = walletService.charge(user, upgradeRequest.getWalletId(), subscriptionPrice, chargeDescription);
        if (charge.getStatus() == TransactionStatus.FAILED) {
            log.warn("Failed charge for subscription for user with id [%s], subscription type [%s]".formatted(user.getId(), subscriptionType));
            return charge;
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime completedOn = subscriptionPeriod == SubscriptionPeriod.MONTHLY ? now.plusMonths(1) : now.plusYears(1);

        Subscription newSubscription = Subscription.builder()
                .owner(user)
                .status(SubscriptionStatus.ACTIVE)
                .period(subscriptionPeriod)
                .type(subscriptionType)
                .price(subscriptionPrice)
                .renewalAllowed(subscriptionPeriod == SubscriptionPeriod.MONTHLY)
                .createdOn(now)
                .completedOn(completedOn)
                .build();
        subscriptionRepository.save(newSubscription);

        currentSubscription.setCompletedOn(now);
        currentSubscription.setStatus(SubscriptionStatus.COMPLETED);
        subscriptionRepository.save(currentSubscription);

        return charge;
    }

    private BigDecimal getSubscriptionPrice(SubscriptionType subscriptionType, SubscriptionPeriod subscriptionPeriod) {
        return switch (subscriptionType) {
            case DEFAULT -> BigDecimal.ZERO;
            case PREMIUM -> switch (subscriptionPeriod) {
                case MONTHLY -> new BigDecimal("19.99");
                case YEARLY -> new BigDecimal("199.99");
            };
            case ULTIMATE -> switch (subscriptionPeriod) {
                case MONTHLY -> new BigDecimal("49.99");
                case YEARLY -> new BigDecimal("499.99");
            };
        };
    }

    public List<Subscription> getAllSubscriptionsReadyForRenewal() {
        return subscriptionRepository.findAllByStatusAndCompletedOnLessThanEqual(SubscriptionStatus.ACTIVE, LocalDateTime.now());
    }

    public void markSubscriptionAsCompleted(Subscription subscription) {

        subscription.setStatus(SubscriptionStatus.COMPLETED);
        subscription.setCompletedOn(LocalDateTime.now());

        subscriptionRepository.save(subscription);
    }

    public void markSubscriptionAsTerminated(Subscription subscription) {

        subscription.setStatus(SubscriptionStatus.TERMINATED);
        subscription.setCompletedOn(LocalDateTime.now());

        subscriptionRepository.save(subscription);
    }
}
