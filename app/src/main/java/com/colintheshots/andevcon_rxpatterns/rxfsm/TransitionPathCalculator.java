package com.colintheshots.andevcon_rxpatterns.rxfsm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;


public class TransitionPathCalculator {
    public static TransitionPath calculateTransitionPath(List<State> sourceStateConfiguration,
                                                         List<State> targetStateConfiguration) {
        if (sourceStateConfiguration == null || targetStateConfiguration == null){
            return new TransitionPath(new ArrayList<State>(),  new ArrayList<State>());
        }

        ListIterator<State> sourceConfigIterator = sourceStateConfiguration.listIterator();
        ListIterator<State> targetConfigIterator = targetStateConfiguration.listIterator();

        while (sourceConfigIterator.hasNext() && targetConfigIterator.hasNext())
        {
            State nextSourceState = sourceConfigIterator.next();
            State nextTargetState = targetConfigIterator.next();

            if (nextSourceState != nextTargetState)
            {
                List<State> toExit = iteratorToList(nextSourceState, sourceConfigIterator);
                Collections.reverse(toExit); // exit states in reverse order
                List<State> toEnter = iteratorToList(nextTargetState, targetConfigIterator);
                return new TransitionPath(toExit, toEnter);
            }
        }

        return new TransitionPath(new ArrayList<State>(),  new ArrayList<State>());
    }

    private static <T> ArrayList<T> iteratorToList(T head, ListIterator<T> rest)
    {
        ArrayList<T> list = new ArrayList<T>();

        list.add(head);

        while (rest.hasNext())
        {
            list.add(rest.next());
        }

        return list;
    }
}
