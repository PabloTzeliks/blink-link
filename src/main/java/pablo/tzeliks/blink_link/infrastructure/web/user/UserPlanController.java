package pablo.tzeliks.blink_link.infrastructure.web.user;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pablo.tzeliks.blink_link.application.user.dto.ChangePlanRequest;
import pablo.tzeliks.blink_link.application.user.usecase.ChangeUserPlanUseCase;
import pablo.tzeliks.blink_link.domain.user.model.Plan;
import pablo.tzeliks.blink_link.infrastructure.web.dto.ErrorResponse;

import java.util.UUID;

@RestController
@RequestMapping("/api/v3/users")
@Tag(name = "User Management", description = "Endpoints for managing user subscriptions and plans")
public class UserPlanController {

    private final ChangeUserPlanUseCase changeUserPlanUseCase;

    public UserPlanController(ChangeUserPlanUseCase changeUserPlanUseCase) {
        this.changeUserPlanUseCase = changeUserPlanUseCase;
    }

    @Operation(summary = "Change User Plan", description = "Updates the subscription plan of a specific user. Requires ADMIN privileges.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "User plan successfully updated"),
            @ApiResponse(responseCode = "400", description = "Invalid Argument - Plan mapping failed",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Valid JWT token missing",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires ADMIN role",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Resource Not Found - User does not exist",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "422", description = "Validation Failed - Invalid request body",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PatchMapping("/{id}/plan")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> changePlan(
            @Parameter(description = "The UUID of the user to update", required = true) @PathVariable UUID id,
            @Valid @RequestBody ChangePlanRequest request) {

        Plan newPlan = Plan.fromString(request.plan());

        changeUserPlanUseCase.execute(id, newPlan);

        return ResponseEntity.noContent().build();
    }
}