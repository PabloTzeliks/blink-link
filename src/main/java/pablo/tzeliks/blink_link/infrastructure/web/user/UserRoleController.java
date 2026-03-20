package pablo.tzeliks.blink_link.infrastructure.web.user;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pablo.tzeliks.blink_link.application.user.usecase.PromoteUserToAdminUseCase;
import pablo.tzeliks.blink_link.application.user.usecase.RevokeUserAdminUseCase;

import java.util.UUID;

@RestController
@RequestMapping("/api/v3/users")
public class UserRoleController {

    private final PromoteUserToAdminUseCase promoteUserToAdminUseCase;
    private final RevokeUserAdminUseCase revokeUserAdminUseCase;

    public UserRoleController(PromoteUserToAdminUseCase promoteUserToAdminUseCase,
                              RevokeUserAdminUseCase revokeUserAdminUseCase) {

        this.promoteUserToAdminUseCase = promoteUserToAdminUseCase;
        this.revokeUserAdminUseCase = revokeUserAdminUseCase;
    }

    @PatchMapping("/{id}/promote")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> promoteToAdmin(@PathVariable UUID id) {

        promoteUserToAdminUseCase.execute(id);

        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/revoke")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> revokeAdmin(@PathVariable UUID id) {

        revokeUserAdminUseCase.execute(id);

        return ResponseEntity.noContent().build();
    }
}