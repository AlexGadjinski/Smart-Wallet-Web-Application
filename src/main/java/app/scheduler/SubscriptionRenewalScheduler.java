package app.scheduler;

import app.subscription.model.Subscription;
import app.subscription.model.SubscriptionPeriod;
import app.subscription.model.SubscriptionType;
import app.subscription.service.SubscriptionService;
import app.transaction.model.Transaction;
import app.transaction.model.TransactionStatus;
import app.user.model.User;
import app.web.dto.UpgradeRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Slf4j
@Component
public class SubscriptionRenewalScheduler {
    private final SubscriptionService subscriptionService;

    public SubscriptionRenewalScheduler(SubscriptionService subscriptionService) {
        this.subscriptionService = subscriptionService;
    }

    @Scheduled(fixedDelay = 20000)
    public void renewSubscriptions() {

        List<Subscription> subscriptions = subscriptionService.getAllSubscriptionsReadyForRenewal();

        if (subscriptions.isEmpty()) {
            log.info("No subscriptions found for renewal.");
            return;
        }

        for (Subscription subscription : subscriptions) {

            User owner = subscription.getOwner();
            if (subscription.isRenewalAllowed()) {

                SubscriptionType subscriptionType = subscription.getType();

                SubscriptionPeriod subscriptionPeriod = subscription.getPeriod();
                UUID walletId = owner.getWallets().get(0).getId();

                UpgradeRequest upgradeRequest = UpgradeRequest.builder()
                        .subscriptionPeriod(subscriptionPeriod)
                        .walletId(walletId)
                        .build();

                Transaction transaction = subscriptionService.upgrade(owner, subscriptionType, upgradeRequest);

                if (transaction.getStatus() == TransactionStatus.FAILED) {
                    subscriptionService.markSubscriptionAsTerminated(subscription);
                    subscriptionService.createDefaultSubscription(owner);
                }

            } else {
                subscriptionService.markSubscriptionAsCompleted(subscription);
                subscriptionService.createDefaultSubscription(owner);
            }
        }
    }
}
