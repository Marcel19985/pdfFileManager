package at.ac.fhtw.swen3.swen3teamm.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

@RestController
public class TestController { //Test für API Endpunkt: gib http://localhost:8080/api/test in browser ein
    @GetMapping("/api/test")
    public Map<String, String> test() {
        return Map.of("message", "Test successful!"); //wird von Spring Boot automatisch in JSON umgewandelt
    }
}
