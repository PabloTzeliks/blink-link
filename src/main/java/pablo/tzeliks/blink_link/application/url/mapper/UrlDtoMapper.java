package pablo.tzeliks.blink_link.application.url.mapper;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import pablo.tzeliks.blink_link.application.url.dto.UrlResponse;
import pablo.tzeliks.blink_link.domain.url.model.Url;

/**
 * @author Pablo Tzeliks
 * @version 2.0.0
 * @since 1.0.0
 */
@Component
public class UrlDtoMapper {

    @Value("${blink-link.base-url}")
    private String baseUrl;

    public UrlResponse toDto(Url domain) {

        String shortUrl = baseUrl.endsWith("/")
                ? baseUrl + domain.getShortCode()
                : baseUrl + "/" + domain.getShortCode();

        return new UrlResponse(
                domain.getUserId(),
                domain.getOriginalUrl(),
                domain.getShortCode(),
                shortUrl,
                domain.getCreatedAt(),
                domain.getExpirationDate()
        );
    }
}
