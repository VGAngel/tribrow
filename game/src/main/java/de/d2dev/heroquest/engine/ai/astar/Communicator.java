package de.d2dev.heroquest.engine.ai.astar;

import java.util.ArrayDeque;

public interface Communicator {

    public ArrayDeque<Knot> getSuccessors(Knot knot);

    public int getTransitionCosts(Knot a, Knot b);

}
