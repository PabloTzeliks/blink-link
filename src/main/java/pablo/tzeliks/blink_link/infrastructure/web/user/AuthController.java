package pablo.tzeliks.blink_link.infrastructure.web.user;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pablo.tzeliks.blink_link.application.user.dto.AuthResponse;
import pablo.tzeliks.blink_link.application.user.dto.LoginUserRequest;
import pablo.tzeliks.blink_link.application.user.dto.RegisterUserRequest;
import pablo.tzeliks.blink_link.application.user.dto.UserResponse;
import pablo.tzeliks.blink_link.application.user.usecase.LoginUserUseCase;
import pablo.tzeliks.blink_link.application.user.usecase.RegisterNewUserUseCase;

@RestController
@RequestMapping("api/v2/auth")
public class AuthController {

    private final RegisterNewUserUseCase registerNewUserUseCase;
    private final LoginUserUseCase loginUserUseCase;

    public AuthController(RegisterNewUserUseCase registerNewUserUseCase,
                          LoginUserUseCase loginUserUseCase) {
        this.registerNewUserUseCase = registerNewUserUseCase;
        this.loginUserUseCase = loginUserUseCase;
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterUserRequest request) {

        UserResponse response = registerNewUserUseCase.execute(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody LoginUserRequest request) {

        AuthResponse response = loginUserUseCase.execute(request);

        return ResponseEntity.ok(response);
    }
}
