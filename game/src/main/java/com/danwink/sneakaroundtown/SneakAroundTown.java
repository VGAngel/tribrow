package com.danwink.sneakaroundtown;

import com.danwink.sneakaroundtown.states.PlayingState;
import com.jme3.app.SimpleApplication;
import com.jme3.bullet.BulletAppState;

public class SneakAroundTown extends SimpleApplication {

    public BulletAppState bulletAppState;

    public void simpleInitApp() {
        //PHYSICS
        bulletAppState = new BulletAppState();
        stateManager.attach(bulletAppState);

        flyCam.setMoveSpeed(50);

        stateManager.attach(new PlayingState());

        stateManager.getState(PlayingState.class).setEnabled(true);
    }

    public static void main(String[] args) {
        SneakAroundTown sat = new SneakAroundTown();
        sat.start();
    }
}
