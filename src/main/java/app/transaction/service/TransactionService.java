package app.transaction.service;

import app.exception.DomainException;
import app.notification.service.NotificationService;
import app.transaction.model.Transaction;
import app.transaction.model.TransactionStatus;
import app.transaction.model.TransactionType;
import app.transaction.repository.TransactionRepository;
import app.user.model.User;
import app.wallet.model.Wallet;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Currency;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class TransactionService {
    private final TransactionRepository transactionRepository;
    private final NotificationService notificationService;

    public TransactionService(TransactionRepository transactionRepository, NotificationService notificationService) {
        this.transactionRepository = transactionRepository;
        this.notificationService = notificationService;
    }

    public Transaction createNewTransaction(User owner, String sender, String receiver, BigDecimal amount,
                                            BigDecimal balanceLeft, Currency currency, TransactionType type,
                                            TransactionStatus status, String description, String failureReason) {
        Transaction transaction = Transaction.builder()
                .owner(owner)
                .sender(sender)
                .receiver(receiver)
                .amount(amount)
                .balanceLeft(balanceLeft)
                .currency(currency)
                .type(type)
                .status(status)
                .description(description)
                .failureReason(failureReason)
                .createdOn(LocalDateTime.now())
                .build();

        String emailBody = failureReason == null
                ? "%s transaction with amount %.2f EUR was successfully processed!".formatted(type, amount.doubleValue())
                : "%s transaction with amount %.2f EUR failed! Reason: %s.".formatted(type, amount.doubleValue(), failureReason);

        notificationService.sendNotification(owner.getId(), "Smart Wallet Transaction", emailBody);

        return transactionRepository.save(transaction);
    }

    public List<Transaction> getAllByOwnerId(UUID ownerId) {
        return transactionRepository.findAllByOwnerIdOrderByCreatedOnDesc(ownerId);
    }

    public Transaction getById(UUID id) {
        return transactionRepository.findById(id)
                .orElseThrow(() -> new DomainException("Transaction with id [%s] does not exist.".formatted(id)));
    }

    public List<Transaction> getAllTransactionsByWallet(Wallet wallet) {

        String walletId = wallet.getId().toString();

        return transactionRepository.findAllBySenderOrReceiverOrderByCreatedOnDesc(walletId, walletId);
    }
}
