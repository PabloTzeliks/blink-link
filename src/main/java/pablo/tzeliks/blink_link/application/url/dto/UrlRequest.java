package pablo.tzeliks.blink_link.application.url.dto;

/**
 * Data Transfer Object (DTO) for incoming URL shortening requests.
 * <p>
 * This record encapsulates the request payload for the URL shortening endpoint,
 * containing only the original URL that needs to be shortened. Using a Java record
 * provides several benefits:
 * <ul>
 *   <li><b>Immutability:</b> Records are inherently immutable, preventing accidental modification</li>
 *   <li><b>Conciseness:</b> Automatically generates constructor, getters, equals, hashCode, and toString</li>
 *   <li><b>Clarity:</b> Clearly signals this is a data carrier with no business logic</li>
 * </ul>
 * <p>
 * <b>Architectural Decision - Why a DTO?</b>
 * <p>
 * Despite the project's preference for simplicity, DTOs are used here to maintain
 * clean separation of concerns:
 * <ul>
 *   <li><b>API Contract Isolation:</b> DTOs decouple the REST API contract from
 *       internal domain models, allowing each to evolve independently</li>
 *   <li><b>Validation Boundary:</b> Provides a dedicated place for request validation
 *       without polluting domain entities</li>
 *   <li><b>Controller Simplification:</b> Controllers work with simple, focused DTOs
 *       rather than complex entity objects</li>
 *   <li><b>Security:</b> Prevents accidental exposure of internal entity fields
 *       through JSON serialization</li>
 *   <li><b>Versioning:</b> DTOs can be versioned independently as the API evolves</li>
 * </ul>
 * <p>
 * This lightweight approach (using Java records) adds minimal complexity while
 * providing significant architectural benefits for the Controller layer.
 *
 * @param url the original long URL to be shortened; expected to be a valid URL string
 * @author Pablo Tzeliks
 * @since 1.0.0
 */
public record UrlRequest(String url) {
}
