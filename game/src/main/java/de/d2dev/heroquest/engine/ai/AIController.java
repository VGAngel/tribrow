package de.d2dev.heroquest.engine.ai;

import java.util.List;

import de.d2dev.heroquest.engine.game.action.GameAction;

public interface AIController {

    public List<GameAction> getActions();
 
}

