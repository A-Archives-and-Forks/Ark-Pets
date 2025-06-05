/** Copyright (c) 2022-2024, Harry Huang
 * At GPL-3.0 License
 */
package cn.harryh.arkpets.guitasks.requests;

import cn.harryh.arkpets.guitasks.GuiTask;
import cn.harryh.arkpets.network.Connections;
import cn.harryh.arkpets.network.NetworkUtils;
import cn.harryh.arkpets.utils.GuiPrefabs;
import cn.harryh.arkpets.utils.Logger;
import cn.harryh.arkpets.utils.StringUtils;
import javafx.concurrent.Task;
import javafx.scene.layout.StackPane;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.URL;
import java.nio.file.Files;

import static cn.harryh.arkpets.Const.httpBufferSizeDefault;
import static cn.harryh.arkpets.Const.httpTimeoutDefault;


/** The task fetches the given remote content and saves it to a specified local path.
 * @implNote Implement the {@link #onDownloadedFile(File)} for future operations to the downloaded file.
 */
abstract public class FetchAsFileTask extends GuiTask {
    protected final String localPath;

    public FetchAsFileTask(StackPane parent, GuiTaskStyle style, String localPath) {
        super(parent, style);
        this.localPath = localPath;
    }

    /** Returns the remote path from which the file will be fetched.
     * @return The remote path to be fetched.
     */
    abstract protected String getRemotePath();

    /** Called when the file has been successfully downloaded.
     * @param file The downloaded file object representing the local file.
     */
    abstract protected void onDownloadedFile(File file);

    @Override
    protected final Task<Boolean> getTask() {
        return new Task<>() {
            @Override
            protected Boolean call() throws Exception {
                URL remoteURL = new URL(getRemotePath());
                File localFile = new File(localPath);

                Logger.info("Network", "Fetching " + remoteURL + " to " + localFile);
                this.updateMessage("正在尝试建立连接");

                NetworkUtils.BufferLog log = new NetworkUtils.BufferLog(httpBufferSizeDefault);
                HttpsURLConnection connection = Connections.createHttpsConnection(remoteURL, httpTimeoutDefault);
                InputStream is = connection.getInputStream();
                OutputStream os = Files.newOutputStream(localFile.toPath());
                BufferedInputStream bis = new BufferedInputStream(is, httpBufferSizeDefault);
                BufferedOutputStream bos = new BufferedOutputStream(os, httpBufferSizeDefault);

                try (bis; bos; is; os) {
                    int len = httpBufferSizeDefault;
                    long sum = 0;
                    long max = connection.getContentLengthLong();
                    byte[] bytes = new byte[len];
                    while ((len = bis.read(bytes)) != -1) {
                        bos.write(bytes, 0, len);
                        sum += len;
                        log.receive();
                        long speed = log.getSpeedPerSecond(500);
                        this.updateMessage("当前已下载：" + StringUtils.getFormattedSizeString(sum) +
                                (speed != 0 ? " (" + StringUtils.getFormattedSizeString(speed) + "/s)" : ""));
                        this.updateProgress(sum, max);
                        if (this.isCancelled()) {
                            this.updateMessage("下载进程已被取消");
                            break;
                        }
                    }
                    this.updateProgress(max, max);
                    bos.flush();
                    Logger.info("Network", "Fetched " + remoteURL + " , size: " + sum);
                }
                return this.isDone() && !this.isCancelled();
            }
        };
    }

    @Override
    protected void onFailed(Throwable e) {
        if (style != GuiTaskStyle.HIDDEN)
            GuiPrefabs.Dialogs.createErrorDialog(parent, e).show();
    }

    @Override
    protected void onSucceeded(boolean result) {
        onDownloadedFile(new File(localPath));
    }
}
