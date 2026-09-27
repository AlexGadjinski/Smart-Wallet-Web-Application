package app;

import app.subscription.model.Subscription;
import app.subscription.model.SubscriptionPeriod;
import app.subscription.model.SubscriptionStatus;
import app.subscription.model.SubscriptionType;
import app.user.model.Country;
import app.user.model.Role;
import app.user.model.User;
import app.wallet.model.Wallet;
import app.wallet.model.WalletStatus;
import lombok.experimental.UtilityClass;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@UtilityClass
public class TestSetup {

    public static User aUser() {
        return User.builder()
                .id(UUID.randomUUID())
                .username("User123")
                .role(Role.USER)
                .country(Country.BULGARIA)
                .isActive(true)
                .createdOn(LocalDateTime.now())
                .subscriptions(List.of(aSubscription()))
                .wallets(List.of(aWallet()))
                .build();
    }

    public static Wallet aWallet() {
        return Wallet.builder()
                .id(UUID.randomUUID())
                .status(WalletStatus.ACTIVE)
                .balance(BigDecimal.ZERO)
                .updatedOn(LocalDateTime.now())
                .build();
    }

    public static Subscription aSubscription() {
        return Subscription.builder()
                .id(UUID.randomUUID())
                .status(SubscriptionStatus.ACTIVE)
                .period(SubscriptionPeriod.MONTHLY)
                .type(SubscriptionType.PREMIUM)
                .price(BigDecimal.ZERO)
                .renewalAllowed(true)
                .completedOn(LocalDateTime.now().plusMonths(1))
                .build();
    }
}
