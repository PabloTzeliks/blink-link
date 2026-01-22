package pablo.tzeliks.blink_link.application.url.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ResolveUrlRequest(
        @JsonProperty("short_url") String shortUrl
) { }
