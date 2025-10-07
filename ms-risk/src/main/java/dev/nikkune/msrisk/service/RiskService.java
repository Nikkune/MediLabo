package dev.nikkune.msrisk.service;

import dev.nikkune.msrisk.client.NotesClient;
import dev.nikkune.msrisk.client.PatientClient;
import dev.nikkune.msrisk.dto.PatientDTO;
import dev.nikkune.msrisk.model.RiskLevel;
import dev.nikkune.msrisk.util.AgeCalculator;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RiskService implements IRiskService {
    private final PatientClient patientClient;
    private final NotesClient notesClient;
    private final List<String> triggerWords;

    public RiskService(PatientClient patientClient, NotesClient notesClient, List<String> triggerWords) {
        this.patientClient = patientClient;
        this.notesClient = notesClient;
        this.triggerWords = triggerWords;
    }

    @Override
    public RiskLevel calculateRiskLevel(String firstName, String lastName) {
        PatientDTO patient = patientClient.patient(firstName, lastName);
        int age = AgeCalculator.ageInYears(patient.getBirthDate());
        boolean isFemale = patient.getGender().equals("F");
        List<String> notes = notesClient.allPatientNotes(firstName, lastName);
        int triggerCount = 0;
        for (String note : notes) {
            String lowerCaseNote = note == null ? "" : note.toLowerCase();
            for (String triggerWord : triggerWords) {
                if (triggerWord == null || triggerWord.isBlank()) continue;
                if (lowerCaseNote.contains(triggerWord.toLowerCase())) {
                    triggerCount++;
                }
            }
        }

        if (triggerCount == 0) return RiskLevel.NONE;

        if (age < 30){
            if (isFemale){
                if (triggerCount >= 7) return RiskLevel.EARLY_ONSET;
                if (triggerCount >= 4) return RiskLevel.IN_DANGER;
            } else {
                if (triggerCount >= 5) return RiskLevel.EARLY_ONSET;
                if (triggerCount >= 3) return RiskLevel.IN_DANGER;
            }
        } else {
            if (triggerCount >= 8) return RiskLevel.IN_DANGER;
            if (triggerCount == 6 || triggerCount == 7) return RiskLevel.IN_DANGER;
            if (triggerCount >= 2) return RiskLevel.BORDERLINE;
        }

        return RiskLevel.NONE;
    }
}
