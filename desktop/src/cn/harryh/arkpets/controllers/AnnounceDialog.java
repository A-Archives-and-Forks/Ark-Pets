/** Copyright (c) 2022-2025, Harry Huang
 * At GPL-3.0 License
 */
package cn.harryh.arkpets.controllers;

import cn.harryh.arkpets.ArkConfig;
import cn.harryh.arkpets.ArkHomeFX;
import cn.harryh.arkpets.guitasks.FetchAnnounceTask;
import cn.harryh.arkpets.guitasks.GuiTask.GuiTaskStyle;
import cn.harryh.arkpets.utils.GuiPrefabs;
import cn.harryh.arkpets.utils.Logger;
import cn.harryh.arkpets.utils.NetUtils;
import cn.harryh.arkpets.utils.markdown.FxmlConvertor;
import cn.harryh.arkpets.utils.markdown.FxmlDocumentController;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;
import com.jfoenix.controls.*;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Group;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.SVGPath;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;


public final class AnnounceDialog implements Controller<ArkHomeFX> {
    @FXML
    public AnchorPane dialog;
    @FXML
    public JFXButton dialogReturn;

    @FXML
    private JFXListView<JFXListCell<AnnounceItem>> annoListView;
    @FXML
    private JFXButton annoRefetch;
    @FXML
    private Label annoTitle;
    @FXML
    private Label annoDate;
    @FXML
    private Label annoGotoOrigin;
    @FXML
    private ScrollPane annoScroll;
    @FXML
    private VBox annoContainer;

    private JFXListCell<AnnounceItem> selectedAnnoCell;

    private AnnounceReadHandler announceReadHandler;

    private ArkHomeFX app;

    @Override
    public void initializeWith(ArkHomeFX app) {
        this.app = app;

        annoListView.getSelectionModel().getSelectedItems().addListener(
                (ListChangeListener<JFXListCell<AnnounceItem>>) (observable -> observable.getList().forEach(
                        (Consumer<JFXListCell<AnnounceItem>>) this::selectCell)
                )
        );

        annoRefetch.setOnAction(e -> this.fetchAnnounce());

        announceReadHandler = new AnnounceReadHandler(app.config);

        Platform.runLater(() -> GuiPrefabs.disableScrollPaneCache(annoScroll));
    }

    public void fetchAnnounce() {
        ObservableList<AnnounceItem> annoItemList = FXCollections.observableArrayList();
        new FetchAnnounceTask(app.body, GuiTaskStyle.COMMON, annoItemList).start();

        annoListView.getItems().clear();
        annoItemList.addListener((ListChangeListener<AnnounceItem>) change -> {
            change.next();
            if (change.wasAdded() && change.getAddedSize() > 0) {
                Logger.info("Announce", "Fetched " + change.getAddedSize() + " announcements");
                change.getAddedSubList().forEach(anno -> annoListView.getItems().add(createCell(anno)));
                announceReadHandler.retainAll(change.getAddedSubList());
            }
            if (!annoListView.getItems().isEmpty()) {
                annoListView.setFixedCellSize(40);
                annoListView.getSelectionModel().select(0);
            }
            annoListView.refresh();
        });
    }

    private JFXListCell<AnnounceItem> createCell(AnnounceItem anno) {
        double width = annoListView.getPrefWidth() * 0.75;
        double offset = width * 0.175;
        JFXListCell<AnnounceItem> cell = new JFXListCell<>();
        cell.getStyleClass().addAll("list-item");
        cell.setPrefWidth(width);
        cell.setItem(anno);
        cell.setId(anno.getAnnoId());
        SVGPath dot = GuiPrefabs.Icons.getIcon(GuiPrefabs.Icons.SVG_DIAMOND, GuiPrefabs.COLOR_WARNING);
        dot.setLayoutX(-offset);
        dot.setScaleX(0.6);
        dot.setScaleY(0.6);
        Label name = new Label(anno.title);
        name.getStyleClass().addAll("list-item-label");
        cell.setGraphic(new Group(dot, name));
        refreshCellGraphic(cell);
        return cell;
    }

    private void refreshCellGraphic(JFXListCell<AnnounceItem> cell) {
        double width = annoListView.getPrefWidth() * 0.75;
        double offset = width * 0.175;
        SVGPath dot = (SVGPath) ((Group) (cell.getGraphic())).getChildrenUnmodifiable().get(0);
        dot.setVisible(!announceReadHandler.isRead(cell.getItem()));
        Label name = (Label) ((Group) (cell.getGraphic())).getChildrenUnmodifiable().get(1);
        name.setPrefWidth(width - (announceReadHandler.isRead(cell.getItem()) ? 0 : offset));
    }

    private void selectCell(JFXListCell<AnnounceItem> cell) {
        // Reset
        if (selectedAnnoCell != null) {
            selectedAnnoCell.getStyleClass().setAll("list-item");
            refreshCellGraphic(selectedAnnoCell);
        }
        selectedAnnoCell = cell;
        selectedAnnoCell.getStyleClass().add("list-item-active");
        // Display info
        AnnounceItem anno = cell.getItem();
        annoTitle.setText(anno.title);
        if (anno.date != null && !anno.date.isBlank()) {
            annoDate.setText(anno.date);
            annoDate.setManaged(true);
            annoDate.setVisible(true);
        } else {
            annoDate.setText("");
            annoDate.setManaged(false);
            annoDate.setVisible(false);
        }
        if (anno.source != null && !anno.source.isBlank()) {
            annoGotoOrigin.setOnMouseClicked(e -> NetUtils.browseWebpage(anno.source));
            annoGotoOrigin.setManaged(true);
            annoGotoOrigin.setVisible(true);
        } else {
            annoGotoOrigin.setOnMouseClicked(null);
            annoGotoOrigin.setManaged(false);
            annoGotoOrigin.setVisible(false);
        }
        // Display announcement
        annoContainer.getChildren().clear();
        FxmlDocumentController document = FxmlConvertor.toFxmlController(anno.markdown);
        document.getBodyNode().setMaxWidth(annoScroll.getWidth());
        document.setHyperlinkConsumer(string -> {
            if (string.startsWith("https://") || string.startsWith("http://")) {
                NetUtils.browseWebpage(string);
            }
        });
        annoContainer.getChildren().add(document.getBodyNode());
        // Mark as read
        announceReadHandler.setRead(anno);
    }


    private static class AnnounceReadHandler {
        public ArkConfig config;

        public AnnounceReadHandler(ArkConfig config) {
            this.config = config;
            if (this.config.user_announcement_read == null)
                this.config.user_announcement_read = new JSONObject();
        }

        public boolean isRead(AnnounceItem anno) {
            if (anno == null)
                return false;
            String annoId = anno.getAnnoId();
            if (config.user_announcement_read.containsKey(annoId)) {
                try {
                    Number timestamp = config.user_announcement_read.getObject(annoId, Number.class);
                    return timestamp != null && timestamp.longValue() > 0;
                } catch (NumberFormatException ignored) {
                }
            }
            return false;
        }

        public void setRead(AnnounceItem anno) {
            String annoId = anno.getAnnoId();
            Logger.debug("Announce", "Read announce id " + annoId);
            config.user_announcement_read.put(annoId, Instant.now().toEpochMilli() / 1000L);
            config.save();
        }

        public void retainAll(List<? extends AnnounceItem> annoList) {
            Set<String> annoIdSet = annoList.stream().map(AnnounceItem::getAnnoId).collect(Collectors.toSet());
            int size = config.user_announcement_read.size();
            if (config.user_announcement_read.keySet().retainAll(annoIdSet)) {
                int delta = size - config.user_announcement_read.size();
                Logger.info("Announce", "Removed " + delta + " legacy local announcement records");
                config.save();
            }
        }
    }


    public static class AnnounceItem {
        /** @since ArkPets 3.7 */ @JSONField
        public String title;
        /** @since ArkPets 3.7 */ @JSONField
        public String date;
        /** @since ArkPets 3.7 */ @JSONField
        public String markdown;
        /** @since ArkPets 3.7 */ @JSONField
        public String source;

        public String getAnnoId() {
            return String.format("%08x", hashCode());
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            AnnounceItem that = (AnnounceItem) o;
            return Objects.equals(title, that.title) && Objects.equals(date, that.date)
                    && Objects.equals(markdown, that.markdown) && Objects.equals(source, that.source);
        }

        @Override
        public int hashCode() {
            return Objects.hash(title, date, markdown, source);
        }
    }
}
