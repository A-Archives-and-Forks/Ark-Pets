/** Copyright (c) 2022-2025, Harry Huang
 * At GPL-3.0 License
 */
package cn.harryh.arkpets.guitasks.requests;

import cn.harryh.arkpets.network.SourceStrategy;
import cn.harryh.arkpets.utils.Logger;
import javafx.scene.layout.StackPane;

import java.io.File;
import java.net.URL;
import java.nio.file.Files;

import static cn.harryh.arkpets.Const.PathConfig;


public class DownloadAppTask extends FetchAsFileTask {
    private SourceStrategy.Source selectedSource;

    public DownloadAppTask(StackPane parent, GuiTaskStyle style) {
        super(parent, style, PathConfig.tempDirPath + "/Installer.exe");
        selectedSource = null;

        try {
            Files.createDirectories(new File(PathConfig.tempDirPath).toPath());
        } catch (Exception e) {
            Logger.warn("Task", "Failed to create temp dir.");
            throw new RuntimeException(e);
        }
    }

    @Override
    protected String getHeader() {
        return "正在下载安装包...";
    }

    @Override
    protected void onFailed(Throwable e) {
        selectedSource.receiveError();
        super.onFailed(e);
    }

    @Override
    protected void onCancelled() {
        selectedSource.receiveError();
        super.onCancelled();
    }

    @Override
    protected URL getTargetURL() {
        selectedSource = SourceStrategy.getStrategy("AppDownload").getBestSource();
        return selectedSource.toURL();
    }

    @Override
    protected void onDownloadedFile(File file) {
    }
}
