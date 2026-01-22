package pablo.tzeliks.blink_link.application.url.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * TODO : new Docs
 *
 * @param url the original long URL to be shortened; expected to be a valid URL string
 * @author Pablo Tzeliks
 * @version 2.0.0
 * @since 1.0.0
 */
public record CreateUrlRequest(
        @JsonProperty("original_url") String originalUrl
) { }
