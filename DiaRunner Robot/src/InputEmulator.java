import java.awt.*;
import java.util.LinkedList;
import java.util.Queue;

public class InputEmulator {
    private Queue<Action> actionQueue = new LinkedList<Action>();
    private Robot robot;
    private int nullCount = 0;

    public InputEmulator() {
        try {
            robot = new Robot();
        } catch (AWTException e) {
            e.printStackTrace();
        }
    }

    public void registerInput(Action act) {
        actionQueue.add(act);
    }

    public void handleInput() {
        while (!actionQueue.isEmpty())
            handleAction(actionQueue.poll());
    }

    private void handleAction(Action a) {
        if (a == null) {
            nullCount++;
            return;
        }
        if (nullCount < 4)
            return;
        nullCount = 0;
        robot.keyPress(a.getKeyCode());
        if (a.isHold())
            robot.delay(a.getHoldDurationInMilliSeconds());
        robot.keyRelease(a.getKeyCode());
    }

}
