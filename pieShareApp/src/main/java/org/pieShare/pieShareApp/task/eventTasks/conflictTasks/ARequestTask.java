/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pieShare.pieShareApp.task.eventTasks.conflictTasks;

import org.pieShare.pieShareApp.model.pieFile.PieFile;
import org.pieShare.pieShareApp.service.requestService.api.IRequestService;
import org.pieShare.pieTools.piePlate.model.message.api.IPieMessage;

/**
 *
 * @author Svetoslav Videnov
 */
public abstract class ARequestTask<T extends IPieMessage> extends ACheckConflictTask<T> {
	
	private IRequestService requestService;

    public void setRequestService(IRequestService requestService) {
        this.requestService = requestService;
    }
	
	protected void doWork(PieFile file) {
		if(!this.isConflicted(file)) {
			this.requestService.requestFile(file);
		}
	}
}
