package digital.zil.hl.module1.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import digital.zil.hl.module1.model.Exhibit;
import digital.zil.hl.module1.service.ExhibitService;

import java.util.List;
import java.util.Map;

@RestController
public class ExhibitController {

    private final ExhibitService exhibitService;

    @Autowired
    public ExhibitController(ExhibitService exhibitService) {
        this.exhibitService = exhibitService;
    }

    @GetMapping("/exhibits")
    public List<Exhibit> getExhibits() {
        return exhibitService.getAllExhibits();
    }


    @GetMapping("/exhibits/{id}")
    public Exhibit getExhibitById(@PathVariable String id) {
        return exhibitService.getExhibitById(id);
    }

    @DeleteMapping("/exhibits/{id}")
    public void deleteExhibit(@PathVariable String id) {
        exhibitService.deleteExhibit(id);
    }

    @PostMapping(value = "/exhibits/")
    public Exhibit saveExhibit(@RequestBody Exhibit exhibit) {
        return exhibitService.saveExhibit(exhibit);
    }

    @PutMapping(value = "/exhibits/{id}")
    public Exhibit updateExhibit(@PathVariable(required = false) String id, @RequestBody Exhibit exhibit) {
        return exhibitService.updateExhibit(id, exhibit);
    }
    @DeleteMapping("/exhibits/clear")
    public void clearExhibits() {
        exhibitService.deleteAll();
    }
}