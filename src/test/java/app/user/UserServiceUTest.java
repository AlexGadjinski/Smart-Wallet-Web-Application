package app.user;

import app.exception.DomainException;
import app.exception.UsernameAlreadyExistException;
import app.notification.service.NotificationService;
import app.security.AuthenticationMetadata;
import app.subscription.model.Subscription;
import app.subscription.service.SubscriptionService;
import app.user.model.Country;
import app.user.model.Role;
import app.user.model.User;
import app.user.repository.UserRepository;
import app.user.service.UserService;
import app.wallet.model.Wallet;
import app.wallet.service.WalletService;
import app.web.dto.RegisterRequest;
import app.web.dto.UserEditRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

// 1. Create the test class
// 2. Annotate the class with @ExtendWith(MockitoExtension.class)
// 3. Get the dependencies of the class you want to test and annotate them with @Mock
// 4. Inject all those dependencies to the class by using @InjectMocks

@ExtendWith(MockitoExtension.class)
public class UserServiceUTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private SubscriptionService subscriptionService;
    @Mock
    private WalletService walletService;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private UserService userService;

    @ParameterizedTest
    @MethodSource("userRolesArguments")
    void whenSwitchRole_thenCorrectRoleIsAssigned(Role currentUserRole, Role expectedUserRole) {
        // GIVEN
        UUID userId = UUID.randomUUID();
        User user = User.builder()
                .role(currentUserRole)
                .build();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // WHEN
        userService.switchRole(userId);

        // THEN
        assertEquals(expectedUserRole, user.getRole());
    }

    private static Stream<Arguments> userRolesArguments() {

        return Stream.of(
                Arguments.of(Role.USER, Role.ADMIN),
                Arguments.of(Role.ADMIN, Role.USER)
        );
    }

    @Test
    void givenExistingUsersInDatabase_whenGetAllUsers_thenReturnThemAll() {
        // GIVEN
        List<User> users = List.of(new User(), new User());
        when(userRepository.findAll()).thenReturn(users);

        // WHEN
        List<User> result = userService.getAllUsers();

        // THEN
        assertThat(result).hasSize(2);
    }

    @Test
    void givenUserWithActiveStatus_whenSwitchStatus_thenUserStatusBecomesInactive() {
        // GIVEN
        User user = User.builder()
                .id(UUID.randomUUID())
                .isActive(true)
                .build();
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        // WHEN
        userService.switchStatus(user.getId());

        // THEN
        assertFalse(user.isActive());
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void givenUserWithInactiveStatus_whenSwitchStatus_thenUserStatusBecomesActive() {
        // GIVEN
        User user = User.builder()
                .id(UUID.randomUUID())
                .isActive(false)
                .build();
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        // WHEN
        userService.switchStatus(user.getId());

        // THEN
        assertTrue(user.isActive());
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void givenExistingUsername_whenRegister_thenExceptionIsThrown() {
        // GIVEN
        RegisterRequest registerRequest = RegisterRequest.builder()
                .username("Vik123")
                .password("123123")
                .country(Country.BULGARIA)
                .build();
        when(userRepository.findByUsername(registerRequest.getUsername())).thenReturn(Optional.of(new User()));

        // WHEN & THEN
        assertThrows(UsernameAlreadyExistException.class, () -> userService.register(registerRequest));
        verify(userRepository, never()).save(any());
        verify(subscriptionService, never()).createDefaultSubscription(any());
        verify(walletService, never()).initializeFirstWallet(any());
        verify(notificationService, never()).saveNotificationPreference(any(UUID.class), anyBoolean(), anyString());
    }

    @Test
    void givenHappyPath_whenRegister() {
        // GIVEN
        RegisterRequest registerRequest = RegisterRequest.builder()
                .username("Vik123")
                .password("123123")
                .country(Country.BULGARIA)
                .build();
        User user = User.builder()
                .id(UUID.randomUUID())
                .build();
        when(userRepository.findByUsername(registerRequest.getUsername())).thenReturn(Optional.empty());
        when(userRepository.save(any())).thenReturn(user);
        when(subscriptionService.createDefaultSubscription(user)).thenReturn(new Subscription());
        when(walletService.initializeFirstWallet(user)).thenReturn(new Wallet());

        // WHEN
        User registeredUser = userService.register(registerRequest);

        // THEN
        assertThat(registeredUser.getSubscriptions()).hasSize(1);
        assertThat(registeredUser.getWallets()).hasSize(1);
        verify(notificationService, times(1)).saveNotificationPreference(user.getId(), false, null);
    }

    @Test
    void givenMissingUserFromDatabase_whenLoadUserByUsername_thenExceptionIsThrown() {
        // GIVEN
        String username = "Vik123";
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        // WHEN & THEN
        assertThrows(DomainException.class, () -> userService.loadUserByUsername(username));
    }

    @Test
    void givenExistingUserInDatabase_whenLoadUserByUsername_thenReturnCorrectAuthenticationMetadata() {
        // GIVEN
        String username = "Vik123";
        User user = User.builder()
                .id(UUID.randomUUID())
                .password("123123")
                .role(Role.ADMIN)
                .isActive(true)
                .build();
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        // WHEN
        UserDetails userDetails = userService.loadUserByUsername(username);

        // THEN
        assertInstanceOf(AuthenticationMetadata.class, userDetails);
        AuthenticationMetadata authenticationMetadata = (AuthenticationMetadata) userDetails;

        assertEquals(user.getId(), authenticationMetadata.getUserId());
        assertEquals(username, authenticationMetadata.getUsername());
        assertEquals(user.getPassword(), authenticationMetadata.getPassword());
        assertEquals(user.getRole(), authenticationMetadata.getRole());
        assertEquals(user.isActive(), authenticationMetadata.isActive());

        assertThat(authenticationMetadata.getAuthorities()).hasSize(1);
        assertEquals("ROLE_ADMIN", authenticationMetadata.getAuthorities().iterator().next().getAuthority());
    }

    @Test
    void givenMissingUserFromDatabase_whenEditUserDetails_thenExceptionsIsThrown() {
        // GIVEN
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        UserEditRequest dto = UserEditRequest.builder().build();

        // WHEN & THEN
        assertThrows(DomainException.class, () -> userService.editUserDetails(userId, dto));
    }

    @Test
    void givenExistingUserInDatabase_whenEditUserDetailsWithActualEmailAndUserHasNoEmail_thenChangeUserDetailsSaveNotificationPreferenceAndSaveToDatabase() {
        // GIVEN
        UUID userId = UUID.randomUUID();
        User user = User.builder().build();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        UserEditRequest dto = UserEditRequest.builder()
                .firstName("Viktor")
                .lastName("Aleksandrov")
                .email("vik123@abv.bg")
                .profilePicture("www.image.com")
                .build();

        // WHEN
        userService.editUserDetails(userId, dto);

        // THEN
        assertEquals("Viktor", user.getFirstName());
        assertEquals("Aleksandrov", user.getLastName());
        assertEquals("vik123@abv.bg", user.getEmail());
        assertEquals("www.image.com", user.getProfilePicture());

        verify(notificationService, times(1)).saveNotificationPreference(userId, true, dto.getEmail());
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void givenExistingUserInDatabase_whenEditUserDetailsWithEmptyEmailAndUserHasAnEmail_thenChangeUserDetailsSaveNotificationPreferenceAndSaveToDatabase() {
        // GIVEN
        UUID userId = UUID.randomUUID();
        User user = User.builder()
                .email("vik123@abv.bg")
                .build();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        UserEditRequest dto = UserEditRequest.builder()
                .firstName("Viktor")
                .lastName("Aleksandrov")
                .email("")
                .profilePicture("www.image.com")
                .build();

        // WHEN
        userService.editUserDetails(userId, dto);

        // THEN
        assertEquals("Viktor", user.getFirstName());
        assertEquals("Aleksandrov", user.getLastName());
        assertEquals("", user.getEmail());
        assertEquals("www.image.com", user.getProfilePicture());

        verify(notificationService, times(1)).saveNotificationPreference(userId, false, null);
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void givenExistingUserInDatabase_whenEditUserDetailsWithActualEmailAndUserHasAnEmail_thenChangeUserDetailsSaveNotificationPreferenceAndSaveToDatabase() {
        // GIVEN
        UUID userId = UUID.randomUUID();
        User user = User.builder()
                .email("vik123@abv.bg")
                .build();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        UserEditRequest dto = UserEditRequest.builder()
                .firstName("Viktor")
                .lastName("Aleksandrov")
                .email("vik123@gmail.com")
                .profilePicture("www.image.com")
                .build();

        // WHEN
        userService.editUserDetails(userId, dto);

        // THEN
        assertEquals("Viktor", user.getFirstName());
        assertEquals("Aleksandrov", user.getLastName());
        assertEquals("vik123@gmail.com", user.getEmail());
        assertEquals("www.image.com", user.getProfilePicture());

        verify(userRepository, times(1)).save(user);
        verify(notificationService, times(1)).saveNotificationPreference(userId, true, dto.getEmail());
    }

    @Test
    void givenExistingUserInDatabase_whenEditUserDetailsWithEmptyEmailAndUserHasNoEmail_thenChangeUserDetailsAndSaveToDatabase() {
        // GIVEN
        UUID userId = UUID.randomUUID();
        User user = User.builder().build();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        UserEditRequest dto = UserEditRequest.builder()
                .firstName("Viktor")
                .lastName("Aleksandrov")
                .email("")
                .profilePicture("www.image.com")
                .build();

        // WHEN
        userService.editUserDetails(userId, dto);

        // THEN
        assertEquals("Viktor", user.getFirstName());
        assertEquals("Aleksandrov", user.getLastName());
        assertEquals("", user.getEmail());
        assertEquals("www.image.com", user.getProfilePicture());

        verify(userRepository, times(1)).save(user);
        verify(notificationService, never()).saveNotificationPreference(any(), anyBoolean(), anyString());
    }

    @Test
    void givenUserWithRoleAdmin_whenSwitchRole_thenUserReceivesUserRole() {
        // GIVEN
        UUID userId = UUID.randomUUID();
        User user = User.builder()
                .id(userId)
                .role(Role.ADMIN)
                .build();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // WHEN
        userService.switchRole(userId);

        // THEN
        assertThat(user.getRole()).isEqualTo(Role.USER);
    }

    @Test
    void givenUserWithRoleUser_whenSwitchRole_thenUserReceivesAdminRole() {
        // GIVEN
        UUID userId = UUID.randomUUID();
        User user = User.builder()
                .id(userId)
                .role(Role.USER)
                .build();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // WHEN
        userService.switchRole(userId);

        // THEN
        assertThat(user.getRole()).isEqualTo(Role.ADMIN);
    }
}
