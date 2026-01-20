package pablo.tzeliks.blink_link.service;

import org.springframework.stereotype.Service;
import pablo.tzeliks.blink_link.logic.Base62Encoder;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class EncoderService {

    private final ConcurrentHashMap urlList = new ConcurrentHashMap<Long, String>();
    private final AtomicLong IdGenerator = new AtomicLong(1000);

    private Base62Encoder encoder;

    public EncoderService(Base62Encoder encoder) {
        this.encoder = encoder;
    }

    public String encode(String urlInput) {

        long id = IdGenerator.incrementAndGet();

        urlList.put(id, urlInput);

        return encoder.encode(id);
    }

    public String decode(String urlInput) {

        long decodedId = encoder.decode(urlInput);

        System.out.println("decoded id: " + decodedId);

        String url = (String) urlList.get(decodedId);

        System.out.println("url decoded: " + url);

        return url;
    }
}
