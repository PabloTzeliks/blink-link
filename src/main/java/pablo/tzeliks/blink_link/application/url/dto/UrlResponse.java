package pablo.tzeliks.blink_link.application.url.dto;

/**
 * Data Transfer Object (DTO) for URL shortening responses.
 * <p>
 * This record encapsulates the response payload returned by the URL shortening endpoint,
 * containing the complete shortened URL that can be shared and used for redirection.
 * Using a Java record provides immutability and automatic generation of necessary methods.
 * <p>
 * <b>Architectural Decision - Why a DTO?</b>
 * <p>
 * While the project aims for architectural simplicity, using DTOs for responses provides
 * important benefits:
 * <ul>
 *   <li><b>API Stability:</b> Response structure remains stable even if internal domain
 *       models change, protecting API consumers from breaking changes</li>
 *   <li><b>Data Shaping:</b> Allows precise control over what data is exposed to clients,
 *       excluding internal fields like database IDs or timestamps</li>
 *   <li><b>Controller Clarity:</b> Controllers return focused, purpose-built response
 *       objects rather than exposing raw entities</li>
 *   <li><b>JSON Structure Control:</b> Defines exactly how the response will be serialized
 *       to JSON, preventing accidental exposure of entity relationships or internal state</li>
 *   <li><b>Documentation:</b> Provides clear, self-documenting API contracts for OpenAPI/Swagger</li>
 * </ul>
 * <p>
 * The DTO pattern, implemented with lightweight Java records, maintains simplicity while
 * ensuring robust separation between internal domain models and external API contracts,
 * which is essential for long-term maintainability and API evolution.
 *
 * @param url the complete shortened URL (e.g., "http://localhost:8080/abc123")
 * @author Pablo Tzeliks
 * @since 1.0.0
 */
public record UrlResponse(String url) {
}
