package digital.slovensko.autogram.core;

import eu.europa.esig.dss.enumerations.SignatureForm;
import eu.europa.esig.dss.enumerations.SignatureLevel;

public enum BaselineLevel {
    B, T, LT, LTA;

    public static BaselineLevel build(SignatureLevel signatureLevel) {
        return BaselineLevel.valueOf(signatureLevel.name().replaceAll(".*_BASELINE_", ""));
    }

    public SignatureLevel getSignatureLevel(SignatureForm signatureForm) {
        return SignatureLevel.valueByName(signatureForm.name() + "_BASELINE_" + this);
    }

    public SignatureLevel getSignatureLevel(SignatureLevel signatureLevel) {
        return getSignatureLevel(signatureLevel.getSignatureForm());
    }
}
