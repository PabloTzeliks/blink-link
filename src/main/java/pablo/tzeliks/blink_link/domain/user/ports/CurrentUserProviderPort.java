package pablo.tzeliks.blink_link.domain.user.ports;

import pablo.tzeliks.blink_link.domain.user.model.Plan;

public interface CurrentUserProviderPort {

    Plan getCurrentUserPlan();
}
