package com.qaldrin.posclient.dto;

/**
 * Customer DTO - matches backend Customer entity
 */
public class CustomerDTO {
    private Long id;
    private String saleId;
    private String contact;
    private String email;

    // Constructors
    public CustomerDTO() {}

    public CustomerDTO(String saleId, String contact, String email) {
        this.saleId = saleId;
        this.contact = contact;
        this.email = email;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSaleId() {
        return saleId;
    }

    public void setSaleId(String saleId) {
        this.saleId = saleId;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String toString() {
        return "CustomerDTO{" +
                "id=" + id +
                ", saleId='" + saleId + '\'' +
                ", contact='" + contact + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}