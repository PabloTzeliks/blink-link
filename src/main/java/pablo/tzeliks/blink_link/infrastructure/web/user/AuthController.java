package pablo.tzeliks.blink_link.infrastructure.web.user;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
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
import pablo.tzeliks.blink_link.infrastructure.web.dto.ErrorResponse;


@RestController
@RequestMapping("api/v3/auth")
@Tag(name = "Authentication", description = "Endpoints for user authentication and registration")
public class AuthController {

    private final RegisterNewUserUseCase registerNewUserUseCase;
    private final LoginUserUseCase loginUserUseCase;

    public AuthController(RegisterNewUserUseCase registerNewUserUseCase,
                          LoginUserUseCase loginUserUseCase) {
        this.registerNewUserUseCase = registerNewUserUseCase;
        this.loginUserUseCase = loginUserUseCase;
    }

    @Operation(summary = "Register a new user", description = "Creates a new user account with the provided credentials.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User successfully registered",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserResponse.class))),
            @ApiResponse(responseCode = "409", description = "Business rule error (e.g., email already exists)",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "422", description = "Validation Failed (e.g., invalid email format or password strength)",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "400", description = "Malformed JSON Request",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterUserRequest request) {

        UserResponse response = registerNewUserUseCase.execute(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Login a user", description = "Authenticates a user and returns a JWT token via an HTTP-Only cookie.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User successfully authenticated",
                    headers = @Header(name = HttpHeaders.SET_COOKIE, description = "Contains the JWT token", schema = @Schema(type = "string")),
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserResponse.class))),
            @ApiResponse(responseCode = "401", description = "Invalid Credentials",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "422", description = "Validation Failed",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/login")
    public ResponseEntity<UserResponse> login(@Valid @RequestBody LoginUserRequest request,
                                              @Value("${spring.security.jwt.cookie-secure:false}") boolean secure) {

        AuthResponse response = loginUserUseCase.execute(request);

        String token = response.token();

        ResponseCookie tokenCookie = ResponseCookie.from("jwt_token", token)
                .httpOnly(true)
                .secure(secure)
                .path("/")
                .maxAge(3600)
                .sameSite("Lax")
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, tokenCookie.toString())
                .body(response.userProfile());
    }

    @Operation(summary = "Logout a user", description = "Clears the authentication token cookie to log the user out.")
    @ApiResponse(responseCode = "200", description = "User successfully logged out",
            headers = @Header(name = HttpHeaders.SET_COOKIE, description = "Clears the JWT token", schema = @Schema(type = "string")))
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@Value("${security.jwt.cookie-secure:false}") boolean secure) {

        ResponseCookie deleteCookie = ResponseCookie.from("jwt_token", "")
                .httpOnly(true)
                .secure(secure)
                .path("/")
                .maxAge(0)
                .sameSite("Lax")
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, deleteCookie.toString())
                .build();
    }
}
