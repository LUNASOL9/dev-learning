package com.example.ticket_api.ai;

public class InvalidAiResponseException extends RuntimeException{

    public InvalidAiResponseException(String message){
        super(message);
    }
}
