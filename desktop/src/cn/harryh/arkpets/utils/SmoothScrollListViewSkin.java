package cn.harryh.arkpets.utils;

import javafx.scene.control.*;
import javafx.scene.control.skin.ListViewSkin;
import javafx.scene.control.skin.VirtualFlow;


public class SmoothScrollListViewSkin<T> extends ListViewSkin<T> {
    public SmoothScrollListViewSkin(ListView<T> listView) {
        super(listView);
        VirtualFlow<ListCell<T>> flow = getVirtualFlow();

        if (!Boolean.TRUE.equals(listView.getProperties().get("no-smooth-scrolling"))) {
            ScrollUtils.addSmoothScrolling(flow, 0.5);
        }
    }

    @Override
    protected double computePrefWidth(double height, double topInset, double rightInset, double bottomInset, double leftInset) {
        return 200;
    }
}
