package digital.slovensko.autogram.core.validation;

import digital.slovensko.autogram.core.SigningJob;
import eu.europa.esig.dss.validation.reports.Reports;

public class ValidationReports {
    private final Reports reports;
    private final SigningJob signingJob;

    public ValidationReports(Reports reports, SigningJob signingJob) {
        this.reports = reports;
        this.signingJob = signingJob;
    }

    public Reports getReports() {
        return reports;
    }

    public SigningJob getSigningJob() {
        return signingJob;
    }

    public boolean haveSignatures() {
        return reports != null && reports.getSimpleReport().getSignaturesCount() > 0;
    }
}
