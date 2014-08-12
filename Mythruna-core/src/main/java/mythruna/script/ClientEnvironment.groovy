package mythruna.script
/**
 *  ${Id}
 *
 *  Copyright (c) 2012, Paul Speed
 *  All rights reserved.
 */

import com.jme3.math.*;

getLocation = {
    return gameClient.getLocation();
}

setLocation = { x,y,z ->
    gameClient.setLocation(x,y,z);
}

Quaternion getFacing() {
    return gameClient.getFacing();
}

float getHeading() {
    float[] array = getFacing().toAngles(new float[3]);
    return array[1];
}
