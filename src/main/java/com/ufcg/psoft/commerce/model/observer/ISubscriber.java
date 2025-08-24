package com.ufcg.psoft.commerce.model.observer;

@FunctionalInterface
public interface ISubscriber {
    void notify(String context);
}
