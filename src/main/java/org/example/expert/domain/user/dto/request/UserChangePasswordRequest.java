package org.example.expert.domain.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserChangePasswordRequest {

    @NotBlank
    private String oldPassword;
    // Dto에서 어노테이션을 사용하여 해결함.
    // 허나 찾아보니 (?=.*\d.*)식은 "'*부분에 공백도 포함할 수 있는' 숫자를 포함해야 한다"는 정규식인데
    // 비밀번호에서 공백을 포함해도 괜찮은건지 고찰이 필요하다고 사료됨.
    @NotBlank(message = "새 비밀번호는 8자 이상이어야 하고, 숫자와 대문자를 포함해야 합니다.")
    @Size(min = 8, message = "새 비밀번호는 8자 이상이어야 하고, 숫자와 대문자를 포함해야 합니다.")
    @Pattern(regexp = "(?=.*\\d.*)(?=.*[A-Z].*)", message = "새 비밀번호는 8자 이상이어야 하고, 숫자와 대문자를 포함해야 합니다.")
    private String newPassword;
}
