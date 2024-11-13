package digital.slovensko.autogram.core;

import eu.europa.esig.dss.spi.x509.tsp.CompositeTSPSource;

import java.util.ArrayList;
import java.util.List;

public class Settings {
    protected boolean en319132;
    protected boolean plainXmlEnabled;
    protected boolean signIndividually;
    protected boolean signaturesValidity;
    protected boolean pdfaCompliance;
    protected boolean expiredCertsEnabled;
    protected List<String> trustedList;
    protected CompositeTSPSource tspSource;


    public void setTrustedList(String trustedList) {
        this.trustedList = trustedList == null ? new ArrayList<>() : new ArrayList<>(List.of(trustedList.split(",")));
    }

    public boolean isPlainXmlEnabled() {
        return plainXmlEnabled;
    }

    public void setPlainXmlEnabled(boolean value) {
        this.plainXmlEnabled = value;
    }

    public boolean isEn319132() {
        return en319132;
    }

    public void setEn319132(boolean en319132) {
        this.en319132 = en319132;
    }

    public boolean isSignIndividually() {
        return signIndividually;
    }

    public void setSignIndividually(boolean signIndividually) {
        this.signIndividually = signIndividually;
    }

    public boolean isSignaturesValidity() {
        return signaturesValidity;
    }

    public void setSignaturesValidity(boolean signaturesValidity) {
        this.signaturesValidity = signaturesValidity;
    }

    public boolean isPdfaCompliance() {
        return pdfaCompliance;
    }

    public void setPdfaCompliance(boolean pdfaCompliance) {
        this.pdfaCompliance = pdfaCompliance;
    }

    public boolean isExpiredCertsEnabled() {
        return expiredCertsEnabled;
    }

    public void setExpiredCertsEnabled(boolean expiredCertsEnabled) {
        this.expiredCertsEnabled = expiredCertsEnabled;
    }

    public List<String> getTrustedList() {
        return trustedList;
    }

    public void addToTrustedList(String country) {
        trustedList.add(country);
    }

    public void removeFromTrustedList(String country) {
        trustedList.remove(country);
    }

    public void setTspSource(CompositeTSPSource tspSource) {
        this.tspSource = tspSource;
    }

    public CompositeTSPSource getTspSource() {
        return tspSource;
    }
}
