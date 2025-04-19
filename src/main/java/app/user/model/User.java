package app.user.model;

import app.subscription.model.Subscription;
import app.wallet.model.Wallet;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true, nullable = false)
    private String username;

    @Basic
    private String firstName;

    @Basic
    private String lastName;

    @Basic
    private String profilePicture;

    @Column(unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Country country;

    @Basic
    private boolean isActive;

    @Column(nullable = false)
    private LocalDateTime createdOn;

    @Column(nullable = false)
    private LocalDateTime updatedOn;

    @OneToMany(mappedBy = "owner", targetEntity = Subscription.class, fetch = FetchType.EAGER)
    @OrderBy("createdOn DESC")
    private List<Subscription> subscriptions = new ArrayList<>();

    @OneToMany(mappedBy = "owner", targetEntity = Wallet.class, fetch = FetchType.EAGER)
    @OrderBy("createdOn ASC")
    private List<Wallet> wallets = new ArrayList<>();
}
