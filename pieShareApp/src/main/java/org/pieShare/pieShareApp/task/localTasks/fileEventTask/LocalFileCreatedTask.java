/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pieShare.pieShareApp.task.localTasks.fileEventTask;

import java.io.IOException;
import org.pieShare.pieShareApp.model.message.api.IFilderMessageBase;
import org.pieShare.pieShareApp.model.pieFilder.PieFilder;
import org.pieShare.pieShareApp.model.pieFilder.PieFile;
import org.pieShare.pieShareApp.model.pieFilder.PieFolder;
import org.pieShare.pieTools.pieUtilities.service.pieLogger.PieLogger;

/**
 *
 * @author Richard
 */
public class LocalFileCreatedTask extends ALocalFileEventTask {

	@Override
	public void run() {
		try {
			PieFilder pieFilder = this.prepareWork();
			IFilderMessageBase msg = null;

			if(pieFilder == null || this.file == null) {
				PieLogger.info(this.getClass(), "Skipping new file: null");
				return;
			}

			if (this.file.isDirectory()) {
                            msg = this.messageFactoryService.getNewFolderMessage();  
                            //TODO History Service for folder!
				PieLogger.info(this.getClass(), "It's a Folder!");

			} else if (this.file.isFile()) {
				msg = this.messageFactoryService.getNewFileMessage();
                            this.historyService.syncPieFileWithDb(((PieFile)pieFilder));
				PieLogger.info(this.getClass(), "It's a File!");
				//TODO: History Service for folder?
			} else {
				PieLogger.info(this.getClass(), "Skipping new file: unknown type (neither file nor folder)");
			}

			super.doWork(msg, pieFilder);

		} catch (IOException ex) {
			//todo: ex handling
		}
	}
}
