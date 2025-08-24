package com.ufcg.psoft.commerce.exception.notification;

public class EventManagerNotSetException extends RuntimeException {
    public EventManagerNotSetException() {super("EventManager is not set in AssetModel");}
}
