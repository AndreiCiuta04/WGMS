package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.Objects;

public class UserVerifyDto {

    @NotNull(message = "Email must not be null")
    @Email
    private String email;

    @NotNull(message = "Verification code must not be null")
    @Size(min = 6, max = 6, message = "Verification code must be exactly 6 characters")
    private String code;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof UserVerifyDto that)) {
            return false;
        }
        return Objects.equals(email, that.email) && Objects.equals(code, that.code);
    }

    @Override
    public int hashCode() {
        return Objects.hash(email, code);
    }

    @Override
    public String toString() {
        return "UserVerifyDto{"
            + "email='" + email + '\''
            + '}';
    }
}
