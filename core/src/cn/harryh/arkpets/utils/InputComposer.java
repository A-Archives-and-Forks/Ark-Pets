/** Copyright (c) 2022-2025, Harry Huang, Litwak913
 * At GPL-3.0 License
 */
package cn.harryh.arkpets.utils;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.utils.IntMap;


public class InputComposer {
    // Mouse
    protected int mouseX = 0;
    protected int mouseY = 0;
    protected int mouseButton = 0;
    protected int mouseIntention = 0;
    protected boolean isMouseDragging = false;
    protected boolean isMouseDown = false;

    // Keyboard
    protected boolean isCtrlPressed;
    protected boolean isLeftPressed;
    protected boolean isRightPressed;
    protected boolean isUpPressed;
    protected boolean isDownPressed;

    protected final IntMap<Runnable> keyHandlerMap = new IntMap<>();

    public void updateIntentionX(int newX) {
        int t = (int) Math.signum(newX - mouseX);
        mouseIntention = t == 0 ? mouseIntention : t;
    }

    public void updatePosition(int newX, int newY) {
        mouseX = newX;
        mouseY = newY;
    }

    public void setMouseDragging(boolean value) {
        isMouseDragging = value;
    }

    public void updateMouseDown(int newX, int newY, int button) {
        mouseX = newX;
        mouseY = newY;
        mouseButton = button;
        isMouseDown = true;
    }

    public void updateMouseUp(int newX, int newY, int button) {
        mouseX = newX;
        mouseY = newY;
        mouseButton = button;
        isMouseDown = false;
    }

    public int getMouseX() {
        return mouseX;
    }

    public int getMouseY() {
        return mouseY;
    }

    public int getMouseButton() {
        return mouseButton;
    }

    public int getMouseIntention() {
        return mouseIntention;
    }

    public boolean isMouseDragging() {
        return isMouseDragging;
    }

    public boolean isMouseDown() {
        return isMouseDown;
    }

    public void handleKeyDown(int keycode) {
        switch (keycode) {
            case Input.Keys.CONTROL_LEFT, Input.Keys.CONTROL_RIGHT -> isCtrlPressed = true;
            case Input.Keys.LEFT -> isLeftPressed = true;
            case Input.Keys.RIGHT -> isRightPressed = true;
            case Input.Keys.UP -> isUpPressed = true;
            case Input.Keys.DOWN -> isDownPressed = true;
        }
    }

    public void handleKeyUp(int keycode) {
        switch (keycode) {
            case Input.Keys.CONTROL_LEFT, Input.Keys.CONTROL_RIGHT -> isCtrlPressed = false;
            case Input.Keys.LEFT -> isLeftPressed = false;
            case Input.Keys.RIGHT -> isRightPressed = false;
            case Input.Keys.UP -> isUpPressed = false;
            case Input.Keys.DOWN -> isDownPressed = false;
        }
    }

    public void handleKeyTyped(char character) {
        Runnable handler = keyHandlerMap.get(character);
        if (handler != null)
            handler.run();
    }

    public void registerKeyTyped(char character, Runnable handler) {
        keyHandlerMap.put(character, handler);
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
