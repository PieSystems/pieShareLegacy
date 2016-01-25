/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pieShare.pieShareApp.task.localTasks.folderEventTask;

import org.pieShare.pieShareApp.model.pieFilder.PieFolder;
import org.pieShare.pieShareApp.service.folderService.IFolderService;
import org.pieShare.pieShareApp.service.historyService.IHistoryService;
import org.pieShare.pieShareApp.task.localTasks.ALocalEventTask;
import org.pieShare.pieTools.pieUtilities.service.pieLogger.PieLogger;

/**
 *
 * @author daniela
 */
public abstract class ALocalFolderEventTask extends ALocalEventTask {

    protected IFolderService folderService;

    public void setFolderService(IFolderService folderService) {
        this.folderService = folderService;
    }
	
    protected PieFolder prepareWork() {
        PieLogger.info(this.getClass(), "It's a Folder!");
		
		PieFolder folder = this.folderService.getPieFolder(file);
		this.historyService.syncPieFolder(folder);
		return folder;
    }

}
