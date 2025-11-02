package app.wallet.service;

import app.exception.DomainException;
import app.subscription.model.Subscription;
import app.subscription.model.SubscriptionType;
import app.transaction.model.Transaction;
import app.transaction.model.TransactionStatus;
import app.transaction.model.TransactionType;
import app.transaction.service.TransactionService;
import app.user.model.User;
import app.wallet.model.Wallet;
import app.wallet.model.WalletStatus;
import app.wallet.repository.WalletRepository;
import app.web.dto.PaymentNotificationEvent;
import app.web.dto.TransferRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
public class WalletService {
    private static final String SMART_WALLET_LTD = "Smart Wallet Ltd";
    private final WalletRepository walletRepository;
    private final TransactionService transactionService;
    private final ApplicationEventPublisher eventPublisher;

    public WalletService(WalletRepository walletRepository, TransactionService transactionService, ApplicationEventPublisher eventPublisher) {
        this.walletRepository = walletRepository;
        this.transactionService = transactionService;
        this.eventPublisher = eventPublisher;
    }

    public void unlockNewWallet(User user) {

        List<Wallet> allUserWallets = walletRepository.findAllByOwnerUsername(user.getUsername());
        Subscription activeSubscription = user.getSubscriptions().get(0);

        boolean isDefaultPlanAndMaxWalletsUnlocked = activeSubscription.getType() == SubscriptionType.DEFAULT && allUserWallets.size() == 1;
        boolean isPremiumPlanAndMaxWalletsUnlocked = activeSubscription.getType() == SubscriptionType.PREMIUM && allUserWallets.size() == 2;
        boolean isUltimatePlanAndMaxWalletsUnlocked = activeSubscription.getType() == SubscriptionType.ULTIMATE && allUserWallets.size() == 3;

        if (isDefaultPlanAndMaxWalletsUnlocked || isPremiumPlanAndMaxWalletsUnlocked || isUltimatePlanAndMaxWalletsUnlocked) {
            throw new DomainException("Max wallets count reached for user with id [%s] and subscription type [%s]."
                    .formatted(user.getId(), activeSubscription.getType()));
        }

        LocalDateTime now = LocalDateTime.now();
        Wallet newWallet = Wallet.builder()
                .owner(user)
                .status(WalletStatus.ACTIVE)
                .balance(new BigDecimal(0))
                .currency(Currency.getInstance("EUR"))
                .createdOn(now)
                .updatedOn(now)
                .build();

        walletRepository.save(newWallet);
    }

    public Wallet initializeFirstWallet(User user) {

        List<Wallet> allUserWallets = walletRepository.findAllByOwnerUsername(user.getUsername());
        if (!allUserWallets.isEmpty()) {
            throw new DomainException("User with id [%s] already has wallets. First wallet can't be initialized.".formatted(user.getId()));
        }

        Wallet wallet = walletRepository.save(initializeWallet(user));
        log.info("Successfully created new wallet with id [%s] and balance [%.2f]".formatted(
                wallet.getId(), wallet.getBalance()
        ));
        return wallet;
    }

    @Transactional
    public Transaction topUp(UUID walletId, BigDecimal amount) {
        Wallet wallet = getById(walletId);
        String transactionDescription = "Top up %.2f".formatted(amount.doubleValue());

        if (wallet.getStatus() == WalletStatus.INACTIVE) {
            return transactionService.createNewTransaction(wallet.getOwner(), SMART_WALLET_LTD, walletId.toString(),
                    amount, wallet.getBalance(), wallet.getCurrency(), TransactionType.DEPOSIT,
                    TransactionStatus.FAILED, transactionDescription, "Inactive wallet");
        }

        wallet.setBalance(wallet.getBalance().add(amount));
        wallet.setUpdatedOn(LocalDateTime.now());
        walletRepository.save(wallet);

        return transactionService.createNewTransaction(wallet.getOwner(), SMART_WALLET_LTD, walletId.toString(),
                amount, wallet.getBalance(), wallet.getCurrency(), TransactionType.DEPOSIT,
                TransactionStatus.SUCCEEDED, transactionDescription, null);
    }

    @Transactional
    public Transaction transferFunds(User sender, TransferRequest transferRequest) {
        Wallet senderWallet = getById(transferRequest.getFromWalletId());
        String transferDescription = "Transfer from %s to %s for %.2f EUR".formatted(
                sender.getUsername(), transferRequest.getToUsername(), transferRequest.getAmount());
        Optional<Wallet> optionalReceiverWallet = walletRepository.findAllByOwnerUsername(transferRequest.getToUsername()).stream()
                .filter(w -> w.getStatus() == WalletStatus.ACTIVE)
                .findFirst();

        if (optionalReceiverWallet.isEmpty()) {
            return transactionService.createNewTransaction(
                    sender, senderWallet.getId().toString(), transferRequest.getToUsername(),
                    transferRequest.getAmount(), senderWallet.getBalance(), senderWallet.getCurrency(), TransactionType.WITHDRAWAL,
                    TransactionStatus.FAILED, transferDescription, "Invalid criteria for transfer");
        }

        Transaction withdrawal =
                charge(sender, senderWallet.getId(), transferRequest.getAmount(), transferDescription);
        if (withdrawal.getStatus() == TransactionStatus.FAILED) {
            return withdrawal;
        }

        Wallet receiverWallet = optionalReceiverWallet.get();
        receiverWallet.setBalance(receiverWallet.getBalance().add(transferRequest.getAmount()));
        receiverWallet.setUpdatedOn(LocalDateTime.now());
        walletRepository.save(receiverWallet);
        transactionService.createNewTransaction(
                receiverWallet.getOwner(), sender.getUsername(), receiverWallet.getId().toString(),
                transferRequest.getAmount(), receiverWallet.getBalance(), receiverWallet.getCurrency(), TransactionType.DEPOSIT,
                TransactionStatus.SUCCEEDED, transferDescription, null);

        return withdrawal;
    }

    @Transactional
    public Transaction charge(User user, UUID walletId, BigDecimal amount, String description) {
        Wallet wallet = getById(walletId);

        if (wallet.getStatus() == WalletStatus.INACTIVE) {
            return transactionService.createNewTransaction(
                    user, wallet.getId().toString(), SMART_WALLET_LTD,
                    amount, wallet.getBalance(), wallet.getCurrency(), TransactionType.WITHDRAWAL,
                    TransactionStatus.FAILED, description, "Inactive wallet"
            );
        }

        if (wallet.getBalance().compareTo(amount) < 0) {
            return transactionService.createNewTransaction(
                    user, wallet.getId().toString(), SMART_WALLET_LTD,
                    amount, wallet.getBalance(), wallet.getCurrency(), TransactionType.WITHDRAWAL,
                    TransactionStatus.FAILED, description, "Insufficient funds");
        }

        wallet.setBalance(wallet.getBalance().subtract(amount));
        wallet.setUpdatedOn(LocalDateTime.now());
        walletRepository.save(wallet);

        PaymentNotificationEvent event = PaymentNotificationEvent.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .amount(amount)
                .paymentTime(LocalDateTime.now())
                .build();
        eventPublisher.publishEvent(event);

        return transactionService.createNewTransaction(
                user, wallet.getId().toString(), SMART_WALLET_LTD,
                amount, wallet.getBalance(), wallet.getCurrency(), TransactionType.WITHDRAWAL,
                TransactionStatus.SUCCEEDED, description, null
        );
    }

    private Wallet getById(UUID id) {
        return walletRepository.findById(id)
                .orElseThrow(() -> new DomainException("Wallet with id [%s] does not exist.".formatted(id)));
    }

    private Wallet initializeWallet(User user) {
        LocalDateTime now = LocalDateTime.now();

        return Wallet.builder()
                .owner(user)
                .status(WalletStatus.ACTIVE)
                .balance(new BigDecimal("20.00"))
                .currency(Currency.getInstance("EUR"))
                .createdOn(now)
                .updatedOn(now)
                .build();
    }

    public Map<UUID, List<Transaction>> getLastFourTransactionsPerWallet(List<Wallet> wallets) {

        // последните 4 транзакции, за които портфейла е бил или изпращач, или получател,
        // като този портфейл принадлежи на конкретния потребител, а не на някой друг
        Map<UUID, List<Transaction>> transactionsByWalletId = new LinkedHashMap<>();

        wallets.forEach(w -> {
            List<Transaction> lastFourTransactions = transactionService.getAllTransactionsByWallet(w).stream()
                    .filter(t -> t.getStatus() == TransactionStatus.SUCCEEDED)
                    .limit(4)
                    .toList();

            transactionsByWalletId.put(w.getId(), lastFourTransactions);
        });

        return transactionsByWalletId;
    }

    public void switchStatus(UUID walletId, UUID ownerId) {

        Optional<Wallet> optionalWallet = walletRepository.findByIdAndOwnerId(walletId, ownerId);
        if (optionalWallet.isEmpty()) {
            throw new DomainException("Wallet with id [%s] does not belong to user with id [%s].".formatted(walletId, ownerId));
        }

        Wallet wallet = optionalWallet.get();
        wallet.setStatus(wallet.getStatus() == WalletStatus.ACTIVE ? WalletStatus.INACTIVE : WalletStatus.ACTIVE);
        walletRepository.save(wallet);
    }
}
