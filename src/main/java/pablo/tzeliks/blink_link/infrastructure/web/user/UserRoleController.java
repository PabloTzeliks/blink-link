package pablo.tzeliks.blink_link.infrastructure.web.user;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pablo.tzeliks.blink_link.application.user.usecase.PromoteUserToAdminUseCase;

import java.util.UUID;

@RestController
@RequestMapping("/api/v3/users")
public class UserRoleController {

    private final PromoteUserToAdminUseCase promoteUserToAdminUseCase;

    public UserRoleController(PromoteUserToAdminUseCase promoteUserToAdminUseCase) {
        this.promoteUserToAdminUseCase = promoteUserToAdminUseCase;
    }

    @PatchMapping("/{id}/promote")
    @PreAuthorize("hasRole('ADMIN')") // Fundamental: Apenas admins promovem admins
    public ResponseEntity<Void> promoteToAdmin(@PathVariable UUID id) {

        promoteUserToAdminUseCase.execute(id);

        return ResponseEntity.noContent().build();
    }
}