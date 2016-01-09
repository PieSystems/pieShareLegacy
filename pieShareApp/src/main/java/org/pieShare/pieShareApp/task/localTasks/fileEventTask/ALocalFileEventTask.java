/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pieShare.pieShareApp.task.localTasks.fileEventTask;

import java.io.IOException;
import org.pieShare.pieShareApp.model.pieFilder.PieFilder;
import org.pieShare.pieShareApp.model.pieFilder.PieFile;
import org.pieShare.pieShareApp.model.pieFilder.PieFolder;
import org.pieShare.pieShareApp.service.fileService.api.IFileService;
import org.pieShare.pieShareApp.service.fileService.api.IFileWatcherService;
import org.pieShare.pieShareApp.service.fileService.fileEncryptionService.IFileEncryptionService;
import org.pieShare.pieShareApp.service.historyService.IHistoryService;
import org.pieShare.pieShareApp.task.localTasks.ALocalEventTask;
import org.pieShare.pieTools.pieUtilities.service.pieLogger.PieLogger;

/**
 *
 * @author Svetoslav
 */
public abstract class ALocalFileEventTask extends ALocalEventTask {

    protected IFileService fileService;
    protected IHistoryService historyService;
    protected IFileEncryptionService fileEncrypterService;
    protected IFileWatcherService fileWatcherService;
    protected IFileService historyFileService;

    public ALocalFileEventTask() {
    }

    public void setFileWatcherService(IFileWatcherService fileWatcherService) {
        this.fileWatcherService = fileWatcherService;
    }

    public void setFileEncrypterService(IFileEncryptionService fileEncrypterService) {
        this.fileEncrypterService = fileEncrypterService;
    }

    public void setHistoryService(IHistoryService historyService) {
        this.historyService = historyService;
    }

    public void setFileService(IFileService fileService) {
        PieLogger.info(this.getClass(), "Setting FileService!");
        this.fileService = fileService;
    }

    public void setHistoryFileService(IFileService historyFileService) {
        this.historyFileService = historyFileService;
    }

    protected PieFile prepareWork() throws IOException {
        if (!syncAllowed()) {
            return null;
        }

        if (this.file.isDirectory()) {
            PieLogger.error(this.getClass(), "It's a folder! Why is it here? - Should be FolderEventTask");
            return null;
        }
        
        PieLogger.info(this.getClass(), "It's a File!");

        this.fileService.waitUntilCopyFinished(this.file);

        PieFile pieFile = this.fileService.getPieFile(file);

        PieFile oldPieFile = this.historyFileService.getPieFile(this.file);

        if (oldPieFile != null && oldPieFile.equals(pieFile)) {
            return null;
        }

        if (this.fileWatcherService.isPieFileModifiedByUs(pieFile)) {
            this.fileWatcherService.removePieFileFromModifiedList(pieFile);
            return null;
        }

        return pieFile;
    }

}
