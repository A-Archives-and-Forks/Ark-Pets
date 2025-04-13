package cn.harryh.arkpets.utils;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.utils.IntMap;


public class InputComposer {
    private boolean isCtrlPressed;
    private boolean isLeftPressed;
    private boolean isRightPressed;
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
        return !isCtrlPressed && isLeftPressed;
    }

    public boolean isRightPressed() {
        return !isCtrlPressed && isRightPressed;
    }

    public boolean isCtrlLeftPressed() {
        return isCtrlPressed && isLeftPressed;
    }

    public boolean isCtrlRightPressed() {
        return isCtrlPressed && isRightPressed;
    }
}
