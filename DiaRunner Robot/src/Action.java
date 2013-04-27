/**
 * Copyright (C) {2013} Agens AS. All rights reserved.
 */
public class Action {
    private int keyCode;
    private int duration;

    public Action(int keyCode) {
        this.keyCode = keyCode;
    }

    public void setHoldDurationInMilliSeconds(int duration) {
        this.duration = duration;
    }

    public int getKeyCode() {
        return keyCode;
    }

    public boolean isHold() {
        return duration > 0;
    }

    public int getHoldDurationInMilliSeconds() {
        return duration;
    }
}
