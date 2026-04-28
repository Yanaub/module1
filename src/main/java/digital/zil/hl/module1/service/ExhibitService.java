package digital.zil.hl.module1.service;

import digital.zil.hl.module1.controller.exeption.ExhibitException;
import digital.zil.hl.module1.model.Excursion;
import digital.zil.hl.module1.model.Exhibit;
import digital.zil.hl.module1.repository.ExcursionRepository;
import digital.zil.hl.module1.repository.ExhibitRepository;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static java.lang.String.format;

@Service
public class ExhibitService {

    public static final String EXHIBIT_NOT_FOUND_MSG = "Exhibit with ID %s not found";
    public static final String EXHIBIT_EXISTS_MSG = "Exhibit with ID %s already exists";
    public static final String EXHIBIT_NAME_EXISTS_MSG = "Exhibit with name '%s' already exists";
    public static final String EXHIBIT_HAS_EXCURSIONS_MSG = "Cannot delete exhibit with ID %s because it has excursions";

    private final ExhibitRepository exhibitRepository;
    private final ExcursionRepository excursionRepository;

    public ExhibitService(ExhibitRepository exhibitRepository,
                          ExcursionRepository excursionRepository) {
        this.exhibitRepository = exhibitRepository;
        this.excursionRepository = excursionRepository;
    }

    public List<Exhibit> getAllExhibits() {
        return exhibitRepository.findAll();
    }

    public Exhibit getExhibitById(String id) {
        return exhibitRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new ExhibitException(format(EXHIBIT_NOT_FOUND_MSG, id)));
    }

    public Exhibit saveExhibit(Exhibit exhibit) {
        if (exhibit.getIdentifier() == null) {
            exhibit.setIdentifier(UUID.randomUUID());
        }
        if (exhibitRepository.existsById(exhibit.getIdentifier())) {
            throw new ExhibitException(format(EXHIBIT_EXISTS_MSG, exhibit.getIdentifier()));
        }
        if (exhibitRepository.existsByName(exhibit.getName())) {
            throw new ExhibitException(format(EXHIBIT_NAME_EXISTS_MSG, exhibit.getName()));
        }
        return exhibitRepository.save(exhibit);
    }
    boolean hasExcursionsForExhibit(UUID exhibitId) {
        return excursionRepository.findAll().stream()
                .anyMatch(ex -> ex.getExhibits().stream()
                        .anyMatch(e -> e.getIdentifier().equals(exhibitId)));
    }
    public void deleteExhibit(String id) {
        UUID uuid = UUID.fromString(id);
        if (!exhibitRepository.existsById(uuid)) {
            throw new ExhibitException(format(EXHIBIT_NOT_FOUND_MSG, id));
        }
        if (hasExcursionsForExhibit(uuid)) {
            throw new ExhibitException(format(EXHIBIT_HAS_EXCURSIONS_MSG, id));
        }
        exhibitRepository.deleteById(uuid);
    }

    public Exhibit updateExhibit(String id, Exhibit exhibit) {
        UUID uuid = UUID.fromString(id);
        Exhibit existing = exhibitRepository.findById(uuid)
                .orElseThrow(() -> new ExhibitException(format(EXHIBIT_NOT_FOUND_MSG, id)));

        if (!existing.getName().equals(exhibit.getName())
                && exhibitRepository.existsByNameAndIdentifierNot(exhibit.getName(), uuid)) {
            throw new ExhibitException(format(EXHIBIT_NAME_EXISTS_MSG, exhibit.getName()));
        }
        exhibit.setIdentifier(uuid);
        return exhibitRepository.save(exhibit);
    }


    public void deleteAll() {
        exhibitRepository.deleteAll();
    }
}