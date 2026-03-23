package pablo.tzeliks.blink_link.infrastructure.web.user;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pablo.tzeliks.blink_link.application.user.usecase.PromoteUserToAdminUseCase;
import pablo.tzeliks.blink_link.application.user.usecase.RevokeUserAdminUseCase;
import pablo.tzeliks.blink_link.infrastructure.web.dto.ErrorResponse;

import java.util.UUID;

@RestController
@RequestMapping("/api/v3/users")
@Tag(name = "User Roles", description = "Endpoints for managing user administrative roles")
public class UserRoleController {

    private final PromoteUserToAdminUseCase promoteUserToAdminUseCase;
    private final RevokeUserAdminUseCase revokeUserAdminUseCase;

    public UserRoleController(PromoteUserToAdminUseCase promoteUserToAdminUseCase,
                              RevokeUserAdminUseCase revokeUserAdminUseCase) {
        this.promoteUserToAdminUseCase = promoteUserToAdminUseCase;
        this.revokeUserAdminUseCase = revokeUserAdminUseCase;
    }

    @Operation(summary = "Promote User to Admin", description = "Grants ADMIN privileges to a specific user. Requires ADMIN privileges.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "User successfully promoted to ADMIN"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Valid JWT token missing",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires ADMIN role",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Resource Not Found - User does not exist",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PatchMapping("/{id}/promote")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> promoteToAdmin(
            @Parameter(description = "The UUID of the user to promote", required = true) @PathVariable UUID id) {

        promoteUserToAdminUseCase.execute(id);

        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Revoke Admin Role", description = "Revokes ADMIN privileges from a specific user. Requires ADMIN privileges.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "User ADMIN role successfully revoked"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Valid JWT token missing",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires ADMIN role",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Resource Not Found - User does not exist",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PatchMapping("/{id}/revoke")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> revokeAdmin(
            @Parameter(description = "The UUID of the user to revoke ADMIN privileges from", required = true) @PathVariable UUID id) {

        revokeUserAdminUseCase.execute(id);

        return ResponseEntity.noContent().build();
    }
}