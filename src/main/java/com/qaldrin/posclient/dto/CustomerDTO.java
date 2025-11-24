package com.qaldrin.posclient.dto;

/**
 * DTO for Customer data transfer between client and server
 * ✅ Removed saleId - Customer is identified by contact only
 */
public class CustomerDTO {

    private Long id;
    private String contact;
    private String email;

    // Constructors
    public CustomerDTO() {
    }

    public CustomerDTO(String contact, String email) {
        this.contact = contact;
        this.email = email;
    }

    public CustomerDTO(Long id, String contact, String email) {
        this.id = id;
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
                ", contact='" + contact + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}