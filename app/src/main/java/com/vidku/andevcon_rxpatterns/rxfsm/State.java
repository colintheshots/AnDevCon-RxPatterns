package com.vidku.andevcon_rxpatterns.rxfsm;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Func1;

public class State {

    private final String name;
    private final Action0 onEntry;
	private final Action0 onExit;
    private final List<State> subStates;
    private final State initialSubState;
    private final List<Transition> transitions;

    public State(String name) {
        this.name = name;
        this.onEntry = null;
        this.onExit = null;
        this.transitions = new ArrayList<Transition>();
        this.subStates = new ArrayList<State>();
        this.initialSubState = null;
    }

    public State withOnEntry(Action0 action) {
        if (onEntry == null)
        {
            return new State(name, action, onExit, transitions, initialSubState, subStates);
        }
        else
        {
            throw new IllegalStateException("There can only be one onEntry function");
        }
    }

    public State withOnExit(Action0 action) {
        if (onExit == null)
        {
            return new State(name, onEntry, action, transitions, initialSubState, subStates);
        }
        else
        {
            throw new IllegalStateException("There can only be one onExit function");
        }
    }


    public <T> State withTransition(String pathToTargetState, Observable<T> event, Action1<T> action)
    {
        List<Transition> newTransitions = new ArrayList<Transition>(transitions);
        newTransitions.add(new Transition(pathToTargetState, event, action));
        return new State(name, onEntry, onExit, newTransitions, initialSubState, subStates);
    }

    public <T> State withTransition(String pathToTargetState, Observable<T> event, Action1<T> action, Func1<? super T, Boolean> guard)
    {
        List<Transition> newTransitions = new ArrayList<Transition>(transitions);
        newTransitions.add(new Transition(pathToTargetState, event, action, guard));
        return new State(name, onEntry, onExit, newTransitions, initialSubState, subStates);
    }

    public <T> State withInternalTransition(Observable<T> event, Action1<T> action)
    {
        List<Transition> newTransitions = new ArrayList<Transition>(transitions);
        newTransitions.add(new Transition(event, action));
        return new State(name, onEntry, onExit, newTransitions, initialSubState, subStates);
    }

    public <T> State withInternalTransition(Observable<T> event, Action1<T> action, Func1<? super T, Boolean> guard)
    {
        List<Transition> newTransitions = new ArrayList<Transition>(transitions);
        newTransitions.add(new Transition(event, action, guard));
        return new State(name, onEntry, onExit, newTransitions, initialSubState, subStates);
    }

    public State withInitialSubState(State subState) {
        List<State> newSubStates = new ArrayList<State>(subStates);
        newSubStates.add(subState);
        return new State(name, onEntry, onExit, transitions, subState, newSubStates);
    }

    public State withSubState(State subState) {
        List<State> newSubStates = new ArrayList<State>(subStates);
        newSubStates.add(subState);
        return new State(name, onEntry, onExit, transitions, initialSubState, newSubStates);
    }

    State(String name, Action0 onEntry, Action0 onExit, List<Transition> transitions, State initialSubState, List<State> subStates) {
        if (initialSubState == null && subStates.size() > 0) {
            throw new IllegalStateException("If there are any sub states, one of them has to be the initial sub state");
        }
        this.name = name;
        this.onEntry = onEntry;
        this.onExit = onExit;
        this.transitions = transitions;
        this.subStates = subStates;
        this.initialSubState = initialSubState;
    }

    State(String name, Action0 onEntry, Action0 onExit, List<Transition> transitions) {
        this.name = name;
        this.onEntry = onEntry;
        this.onExit = onExit;
        this.transitions = transitions;
        this.subStates = new ArrayList<State>();
        this.initialSubState = null;
    }

	public void enter() {
		if (onEntry != null)
		{
			onEntry.call();
		}
	}

	public void exit() {
		if (onExit != null)
		{
			onExit.call();
		}
	}

    public List<State> getSubStates() {
        return subStates;
    }

    public State getInitialSubState() {
        return initialSubState;
    }

    public String getName() {
        return name;
    }

    public List<Transition> getTransitions() {
        return transitions;
    }
}
