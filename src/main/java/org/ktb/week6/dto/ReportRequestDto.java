package org.ktb.week6.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.ktb.week6.enums.ReportReason;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ReportRequestDto {

    @NotNull(message = "report_reason_required")
    private ReportReason reason;
}
