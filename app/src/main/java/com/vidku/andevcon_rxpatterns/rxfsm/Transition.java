package com.vidku.andevcon_rxpatterns.rxfsm;

import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;

public class Transition {

    // private final String description;
    private final Object event;
    private final Observable<String> observable;

    public <T> Transition(String pathToTargetState, Observable<T> event, Action1<T> action) {
		this.observable = event
                .map((T t) -> {
                    action.call(t);
                    return pathToTargetState;
                });
        this.event = event;
    }

    public <T> Transition(String pathToTargetState, Observable<T> event, Action1<T> action, Func1<? super T, Boolean> guard) {
        this.observable = event
                .filter(guard)
                .map((T t) -> {
                    action.call(t);
                    return pathToTargetState;
                });
        this.event = event;
    }

    // Internal transition
    public <T> Transition(Observable<T> event, Action1<T> action) {
        this.observable = event
                .map((T t) -> {
                    action.call(t);
                    return null;
                });
        this.event = event;
    }

    // Internal transition
    public <T> Transition(Observable<T> event, Action1<T> action, Func1<? super T, Boolean> guard) {
        this.observable = event
                .filter(guard)
                .map((T t) -> {
                    action.call(t);
                    return null;
                });
        this.event = event;
    }

	Observable<String> observable() {
		return observable;
	}

    Object event() {
        return this.event;
    }
}