package digital.slovensko.autogram.core.validation;

import eu.europa.esig.dss.validation.DocumentValidator;
import eu.europa.esig.dss.validation.reports.Reports;

public record ReportsAndValidator(Reports reports, DocumentValidator validator) {
}
