package digital.zil.hl.module1.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CrashController {

    @PostMapping("/crash")
    public ResponseEntity<String> crash() {
        System.err.println("CRASH ENDPOINT CALLED - Exiting JVM");
        System.exit(1);
        return ResponseEntity.ok("Should not reach here");
    }
}