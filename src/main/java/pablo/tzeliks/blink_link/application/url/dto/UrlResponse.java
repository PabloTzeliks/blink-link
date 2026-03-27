package pablo.tzeliks.blink_link.application.url.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record UrlResponse(

        @JsonProperty("original_url")
        String originalUrl
) { }
