package pablo.tzeliks.blink_link.application.user.ports;

import pablo.tzeliks.blink_link.domain.user.model.Plan;

import java.util.UUID;

public interface CurrentUserProviderPort {

    Plan getCurrentUserPlan();

    UUID getCurrentUserId();
}
