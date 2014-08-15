package com.pro.gui;

import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.screen.ScreenController;

import javax.annotation.Nonnull;

public class HelloNiftySelectController implements ScreenController {

    private Nifty nifty;
    private Screen screen;

    @Override
    public void bind(@Nonnull final Nifty niftyParam, @Nonnull final Screen screenParam) {
        this.nifty = niftyParam;
        this.screen = screenParam;
    }

    public void onStartScreen() {
    }

    public void onEndScreen() {
    }
}
