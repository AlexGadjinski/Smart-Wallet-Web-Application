package app;

import app.subscription.model.Subscription;
import app.subscription.model.SubscriptionPeriod;
import app.subscription.model.SubscriptionStatus;
import app.subscription.model.SubscriptionType;
import app.subscription.repository.SubscriptionRepository;
import app.subscription.service.SubscriptionService;
import app.transaction.model.Transaction;
import app.transaction.model.TransactionStatus;
import app.user.model.Country;
import app.user.model.User;
import app.user.service.UserService;
import app.wallet.model.Wallet;
import app.wallet.repository.WalletRepository;
import app.web.dto.RegisterRequest;
import app.web.dto.UpgradeRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.comparesEqualTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@SpringBootTest
public class SubscribeITest {

    // IMPORTANT: 'user' is a key word in H2, so we should name our table 'users'

    @Autowired
    private UserService userService;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private SubscriptionService subscriptionService;

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Test
    void subscribeToPlan_happyPath() {
        // GIVEN
        RegisterRequest registerRequest = RegisterRequest.builder()
                .username("Vik123")
                .password("123123")
                .country(Country.BULGARIA)
                .build();
        User registeredUser = userService.register(registerRequest);

        UpgradeRequest upgradeRequest = UpgradeRequest.builder()
                .subscriptionPeriod(SubscriptionPeriod.MONTHLY)
                .walletId(registeredUser.getWallets().get(0).getId())
                .build();

        // WHEN
        Transaction transaction = subscriptionService.upgrade(registeredUser, SubscriptionType.PREMIUM, upgradeRequest);

        // THEN
        // 1. Transaction status is SUCCEEDED
        assertThat(transaction.getStatus(), is(TransactionStatus.SUCCEEDED));
//        assertEquals(TransactionStatus.SUCCEEDED, transaction.getStatus());

        // 2. User has one active premium subscription
        Optional<Subscription> subscription = subscriptionRepository.findByOwnerIdAndStatus(registeredUser.getId(), SubscriptionStatus.ACTIVE);
        assertTrue(subscription.isPresent());
        assertThat(subscription.get().getType(), is(SubscriptionType.PREMIUM));

        // 3. User is charged
        Optional<Wallet> wallet = walletRepository.findByIdAndOwnerId(registeredUser.getWallets().get(0).getId(), registeredUser.getId());
        assertTrue(wallet.isPresent());
        assertThat(wallet.get().getBalance(), comparesEqualTo(new BigDecimal("0.01")));
    }
}
