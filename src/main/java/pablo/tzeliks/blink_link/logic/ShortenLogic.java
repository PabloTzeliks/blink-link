package pablo.tzeliks.blink_link.logic;

public interface ShortenLogic {

    String encode(Long id);
    Long decode(String shortCode);
}
