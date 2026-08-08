package app.web.mapper;

import app.user.model.User;
import app.web.dto.UserEditRequest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DtoMapperUTest {

    @Test
    void givenHappyPath_whenMappingUserToUserEditRequest() {
        // GIVEN
        User user = User.builder()
                .firstName("Vik")
                .lastName("Aleksandrov")
                .email("vik123@abv.bg")
                .profilePicture("www.image.com")
                .build();

        // WHEN
        UserEditRequest resultDto = DtoMapper.mapToUserEditRequest(user);

        // THEN
        assertEquals(user.getFirstName(), resultDto.getFirstName());
        assertEquals(user.getLastName(), resultDto.getLastName());
        assertEquals(user.getEmail(), resultDto.getEmail());
        assertEquals(user.getProfilePicture(), resultDto.getProfilePicture());
    }
}
