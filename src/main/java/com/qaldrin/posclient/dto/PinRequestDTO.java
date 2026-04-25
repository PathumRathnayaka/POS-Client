package com.qaldrin.posclient.dto;

/**
 * DTO for sending a PIN to the server for validation.
 * Matches the server's PinRequestDTO.
 * POST /api/password/validate
 */
public class PinRequestDTO {
    private String pin;

    public PinRequestDTO() {
    }

    public PinRequestDTO(String pin) {
        this.pin = pin;
    }

    public String getPin() {
        return pin;
    }

    public void setPin(String pin) {
        this.pin = pin;
    }
}
