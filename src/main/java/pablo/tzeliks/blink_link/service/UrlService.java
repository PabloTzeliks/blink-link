package pablo.tzeliks.blink_link.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import pablo.tzeliks.blink_link.logic.ShortenLogic;
import pablo.tzeliks.blink_link.model.UrlEntity;
import pablo.tzeliks.blink_link.repository.UrlRepository;

@Service
public class UrlService {

    private UrlRepository urlRepository;
    private ShortenLogic encoder;

    public UrlService(UrlRepository urlRepository, ShortenLogic encoder) {
        this.urlRepository = urlRepository;
        this.encoder = encoder;
    }

    @Transactional
    public UrlEntity shorten(String longUrl) {

        if (longUrl == null || longUrl.isEmpty()) { throw new IllegalArgumentException("The URL cannot be empty."); }

        UrlEntity rawUrl = new UrlEntity(longUrl);

        UrlEntity savedUrl = urlRepository.save(rawUrl);

        String shortCode = encoder.encode(savedUrl.getId());
        savedUrl.setShortCode(shortCode);

        return savedUrl;
    }

    public UrlEntity resolve(String shortCode) {

        return urlRepository.findByShortCode(shortCode).orElse(null);
    }
}
