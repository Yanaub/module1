package digital.zil.hl.module1.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal")
public class CrashController {

    @PostMapping("/crash")
    public ResponseEntity<String> crash() {
        new Thread(() -> {
            try {
                Thread.sleep(200);
            } catch (InterruptedException ignored) {
            }
            System.exit(1);
        }).start();
        return ResponseEntity.ok("core service will stop");
    }
}
