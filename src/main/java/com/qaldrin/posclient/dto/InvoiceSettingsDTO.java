package com.qaldrin.posclient.dto;

public class InvoiceSettingsDTO {
    private String companyName;
    private String companySlogan;
    private String companyAddress;
    private String companyContact;
    private String footerMessage1;
    private String footerMessage2;
    private boolean hasLogo;
    private String logoPath;
    private String language;

    public InvoiceSettingsDTO() {
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getCompanySlogan() {
        return companySlogan;
    }

    public void setCompanySlogan(String companySlogan) {
        this.companySlogan = companySlogan;
    }

    public String getCompanyAddress() {
        return companyAddress;
    }

    public void setCompanyAddress(String companyAddress) {
        this.companyAddress = companyAddress;
    }

    public String getCompanyContact() {
        return companyContact;
    }

    public void setCompanyContact(String companyContact) {
        this.companyContact = companyContact;
    }

    public String getFooterMessage1() {
        return footerMessage1;
    }

    public void setFooterMessage1(String footerMessage1) {
        this.footerMessage1 = footerMessage1;
    }

    public String getFooterMessage2() {
        return footerMessage2;
    }

    public void setFooterMessage2(String footerMessage2) {
        this.footerMessage2 = footerMessage2;
    }

    public boolean isHasLogo() {
        return hasLogo;
    }

    public void setHasLogo(boolean hasLogo) {
        this.hasLogo = hasLogo;
    }

    public String getLogoPath() {
        return logoPath;
    }

    public void setLogoPath(String logoPath) {
        this.logoPath = logoPath;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }
}
