package com.colintheshots.andevcon_rxpatterns.rxfsm;

import java.util.List;

public class TransitionPath {
    private final List<State> statesToExit;
    private final List<State> statesToEnter;

    public TransitionPath(List<State> statesToExit, List<State> statesToEnter) {
        this.statesToExit = statesToExit;
        this.statesToEnter = statesToEnter;
    }

    public List<State> getStatesToExit() {
        return statesToExit;
    }

    public List<State> getStatesToEnter() {
        return statesToEnter;
    }
}
