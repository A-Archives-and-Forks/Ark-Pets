/** Copyright (c) 2022-2025, Harry Huang
 * At GPL-3.0 License
 */
package cn.harryh.arkpets.guitasks;

import cn.harryh.arkpets.concurrent.ProcessPool;
import cn.harryh.arkpets.utils.GuiPrefabs;
import cn.harryh.arkpets.utils.Logger;
import javafx.concurrent.Task;
import javafx.scene.layout.StackPane;

import java.io.File;
import java.io.IOException;
import java.util.List;


public class AppInstallTask extends GuiTask {
    protected static final int PRE_DELAY = 500;
    protected static final int POST_DELAY = 5000;

    protected final File pack;

    public AppInstallTask(StackPane parent, GuiTaskStyle style, File pack) {
        super(parent, style);
        this.pack = pack;
    }

    @Override
    protected String getHeader() {
        return "即将开始安装...";
    }

    @Override
    protected Task<Boolean> getTask() {
        return new Task<>() {
            @Override
            protected Boolean call() throws Exception {
                // Use thread sleep here to ensure the dialog is shown
                this.updateMessage("正在准备");
                this.updateProgress(0, 1);
                Thread.sleep(PRE_DELAY);

                // Get the pack file and checks if it is valid
                List<String> command;
                if (pack.isFile() && pack.canExecute() && pack.getName().toLowerCase().endsWith(".exe")) {
                    // Windows only
                    Logger.info("Updater", "Installing app from: " + pack.getAbsolutePath());
                    command = List.of(
                            "cmd.exe",
                            "/c",
                            pack.getAbsolutePath(),
                            "/SILENT",
                            "/NOCANCEL",
                            "/FORCECLOSEAPPLICATIONS",
                            "/RESTARTAPPLICATIONS"
                    );
                } else {
                    Logger.error("Updater", "File not found or invalid: " + pack.getAbsolutePath());
                    throw new IOException("Cannot install due to invalid file");
                }

                // Use countdown to notify the user that the installation is about to start
                int interval = 10;
                for (long i = POST_DELAY; i >= 0; i -= interval) {
                    this.updateMessage("将在 " + (int) Math.ceil(i / 1000.0) + " 秒后开始安装\n安装完成后您可重新打开程序");
                    this.updateProgress(POST_DELAY - i, POST_DELAY);
                    Thread.sleep(interval);
                }

                // Execute the installation command
                ProcessPool.getInstance().submit(command);
                return this.isDone() && !this.isCancelled();
            }
        };
    }

    @Override
    protected void onFailed(Throwable e) {
        if (style != GuiTaskStyle.HIDDEN)
            GuiPrefabs.Dialogs.createErrorDialog(parent, e).show();
    }
}
