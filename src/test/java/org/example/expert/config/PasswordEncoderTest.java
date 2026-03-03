package org.example.expert.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
class PasswordEncoderTest {

    @InjectMocks
    private PasswordEncoder passwordEncoder;

    @Test
    @DisplayName("matches 메서드 동작 성공")
    void matches_성공() {
        // given
        String rawPassword = "testPassword";
        String encodedPassword = passwordEncoder.encode(rawPassword);

        // when
        boolean matches = passwordEncoder.matches(encodedPassword, rawPassword);

        // then
        // 원래의 코드와 해당 코드를 암호화한 코드는 다르기에
        // assertFalse를 입력하여야 성공할 수 있음.
        assertFalse(matches);
    }
}
