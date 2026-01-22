package pablo.tzeliks.blink_link.application.url.mapper;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import pablo.tzeliks.blink_link.application.url.dto.CreateUrlRequest;
import pablo.tzeliks.blink_link.application.url.dto.UrlResponse;
import pablo.tzeliks.blink_link.domain.url.model.Url;

import java.time.LocalDateTime;

@Component
public class UrlDtoMapper {

    @Value("${blink-link.base-url}")
    private String baseUrl;

    public UrlResponse toDto(Url domain) {

        String shortUrl = baseUrl.endsWith("/")
                ? baseUrl + domain.getShortCode()
                : baseUrl + "/" + domain.getShortCode();

        return new UrlResponse(
                domain.getOriginalUrl(),
                shortUrl,
                domain.getCreatedAt()
        );


    }

    public Url toDomain(CreateUrlRequest request, Long id, String shortCode) {

        return new Url(
                id,
                request.originalUrl(),
                shortCode,
                LocalDateTime.now()
        );
    }
}
