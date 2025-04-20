package cn.harryh.arkpets.utils;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.utils.IntMap;


public class InputComposer {
    private boolean isCtrlPressed;
    private boolean isLeftPressed;
    private boolean isRightPressed;
    private boolean isUpPressed;
    private boolean isDownPressed;

    private final IntMap<Runnable> keyMap = new IntMap<>();

    public void handleKeyDown(int keycode) {
        if (keycode == Input.Keys.CONTROL_LEFT || keycode == Input.Keys.CONTROL_RIGHT) {
            isCtrlPressed = true;
        }
        if (keycode == Input.Keys.LEFT) {
            isLeftPressed = true;
        }
        if (keycode == Input.Keys.RIGHT) {
            isRightPressed = true;
        }
        if (keycode == Input.Keys.UP) {
            isUpPressed = true;
        }
        if (keycode == Input.Keys.DOWN) {
            isDownPressed = true;
        }
    }

    public void handleKeyUp(int keycode) {
        if (keycode == Input.Keys.CONTROL_LEFT || keycode == Input.Keys.CONTROL_RIGHT) {
            isCtrlPressed = false;
        }
        if (keycode == Input.Keys.LEFT) {
            isLeftPressed = false;
        }
        if (keycode == Input.Keys.RIGHT) {
            isRightPressed = false;
        }
        if (keycode == Input.Keys.UP) {
            isUpPressed = false;
        }
        if (keycode == Input.Keys.DOWN) {
            isDownPressed = false;
        }
    }

    public void handleKeyTyped(char character) {
        Runnable handler = keyMap.get(character);
        if (handler != null) handler.run();
    }

    public void registerKeyTyped(char character, Runnable handler) {
        keyMap.put(character, handler);
    }

    public boolean isCtrlPressed() {
        return isCtrlPressed;
    }

    public boolean isLeftPressed() {
        return isLeftPressed;
    }

    public boolean isRightPressed() {
        return isRightPressed;
    }

    public boolean isUpPressed() {
        return !isCtrlPressed && isUpPressed;
    }

    public boolean isDownPressed() {
        return !isCtrlPressed && isDownPressed;
    }

    public boolean isCtrlUpPressed() {
        return isCtrlPressed && isUpPressed;
    }

    public boolean isCtrlDownPressed() {
        return isCtrlPressed && isDownPressed;
    }
}
