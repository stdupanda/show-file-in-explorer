package io.github.stdupanda.plugin.intellij;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * show file/folder in the explorer(or finder in macOS)
 */
public class OpenInExplorer extends AnAction {

    public static final String MY_GROUP = "MyPluginNotificationGroup";

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        final Project project = e.getProject();
        if (Objects.isNull(project) || !project.isOpen() || !project.isInitialized()) {
            return;
        }

        // get selected files
        VirtualFile vFile = e.getData(CommonDataKeys.VIRTUAL_FILE);
        VirtualFile[] vFileArr = e.getData(CommonDataKeys.VIRTUAL_FILE_ARRAY);

        // check os
        if (notSupported(project)) {
            return;
        }

        // open all selected
        if (vFileArr != null) {
            List<String> paths = Arrays.stream(vFileArr).map(VirtualFile::getPath).distinct().collect(Collectors.toList());
            for (String path : paths) {
                doAction(path);
            }
            return;
        }

        if (vFile != null) {
            doAction(vFile.getPath());
            return;
        }

        Notifications.Bus.notify(new Notification(MY_GROUP,
                "",
                "Please Select file(s) in project view",
                NotificationType.INFORMATION), project);
    }

    boolean notSupported(Project project) {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.toLowerCase().contains("win") || os.toLowerCase().contains("mac")) {
            return false;
        }
        Notifications.Bus.notify(new Notification(MY_GROUP,
                "",
                "OS not supported, currently only supports Windows and macOS.",
                NotificationType.INFORMATION), project);
        return true;
    }

    void doAction(String path) {
        String command;
        String os = System.getProperty("os.name").toLowerCase();
        if (os.toLowerCase().contains("win")) {
            path = path.replace("/", "\\");
            command = "explorer /e,/select," + path;
        } else if (os.toLowerCase().contains("mac")) {
            command = "open -n -R " + path;
        } else {
            return;
        }

        // call runtime command
        try {
            Runtime.getRuntime().exec(command);
        } catch (IOException ignore) {
        }
    }
}
