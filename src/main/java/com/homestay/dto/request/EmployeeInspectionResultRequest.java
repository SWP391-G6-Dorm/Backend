package com.homestay.dto.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;
import org.springframework.util.StringUtils;

/** SCR-62 - Pass/Fail inspection body. Checklist is UI-only; optional for note summary. */
@Data
public class EmployeeInspectionResultRequest {

    /** API contract field name is {@code note}; {@code notes} accepted for backward compatibility. */
    @JsonAlias("notes")
    private String note;

    private Checklist checklist;

    public String resolveNote() {
        return StringUtils.hasText(note) ? note.trim() : null;
    }

    @Data
    public static class Checklist {
        private Boolean tv;
        private Boolean minibar;
        private Boolean ac;
        private Boolean bathroom;
        private Boolean beds;
    }
}
