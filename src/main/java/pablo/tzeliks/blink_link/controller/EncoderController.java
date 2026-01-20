package pablo.tzeliks.blink_link.controller;

import org.springframework.web.bind.annotation.*;
import pablo.tzeliks.blink_link.service.EncoderService;

@RestController("/")
public class EncoderController {

    private EncoderService encoderService;

//    public EncoderController(EncoderService encoderService) {
//        this.encoderService = encoderService;
//    }
//
//    @PostMapping("/encode")
//    public String encode(@RequestBody String urlInput) {
//
//        return encoderService.shorten(urlInput);
//    }
//
//    @GetMapping("/access/{encodedUrl}")
//    public String access(@PathVariable(name = "encodedUrl") String encodedUrl) {
//
//        return encoderService.resolve(encodedUrl);
//    }
}
