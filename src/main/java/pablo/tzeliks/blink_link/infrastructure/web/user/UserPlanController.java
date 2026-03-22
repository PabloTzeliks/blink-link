package pablo.tzeliks.blink_link.infrastructure.web.user;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pablo.tzeliks.blink_link.application.user.dto.ChangePlanRequest;
import pablo.tzeliks.blink_link.application.user.usecase.ChangeUserPlanUseCase;
import pablo.tzeliks.blink_link.domain.user.model.Plan;

import java.util.UUID;

@RestController
@RequestMapping("/api/v3/users")
public class UserPlanController {

    private final ChangeUserPlanUseCase changeUserPlanUseCase;

    public UserPlanController(ChangeUserPlanUseCase changeUserPlan) {
        this.changeUserPlanUseCase = changeUserPlan;
    }

    @PatchMapping("/{id}/plan")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> changePlan(@PathVariable UUID id, @RequestBody @Valid ChangePlanRequest request) {

        Plan newPlan = Plan.fromString(request.plan());

        changeUserPlanUseCase.execute(id, newPlan);

        return ResponseEntity.noContent().build();
    }
}
