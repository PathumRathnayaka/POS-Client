package com.qaldrin.posclient.dto;

/**
 * DTO for Customer data transfer between client and server.
 * The server returns customerId as a String.
 */
public class CustomerDTO {

    // Server returns customerId as String
    private String id;
    private String contact;
    private String email;

    // Constructors
    public CustomerDTO() {
    }

    public CustomerDTO(String contact, String email) {
        this.contact = contact;
        this.email = email;
    }

    public CustomerDTO(String id, String contact, String email) {
        this.id = id;
        this.contact = contact;
        this.email = email;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
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