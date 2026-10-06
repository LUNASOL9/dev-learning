package com.example.ticket_api;

public record Ticket(
    long id,
    String title,
    String status
) {
    
}