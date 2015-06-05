/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pieShare.pieShareApp.task.eventTasks;

import java.io.File;
import org.pieShare.pieShareApp.model.message.api.IMetaCommitMessage;
import org.pieShare.pieShareApp.service.comparerService.api.ILocalFileCompareService;
import org.pieShare.pieShareApp.service.fileService.api.IFileService;
import org.pieShare.pieShareApp.service.requestService.api.IRequestService;
import org.pieShare.pieShareApp.service.shareService.IBitTorrentService;
import org.pieShare.pieShareApp.service.shareService.IShareService;
import org.pieShare.pieTools.pieUtilities.service.pieLogger.PieLogger;
import org.pieShare.pieTools.pieUtilities.task.PieEventTaskBase;

/**
 *
 * @author Svetoslav
 */
public class MetaCommitTask extends PieEventTaskBase<IMetaCommitMessage> {
	
	private IShareService shareService;
	private IBitTorrentService bitTorrentService;
	private ILocalFileCompareService compareService;
	private IRequestService requestService;

	public void setBitTorrentService(IBitTorrentService bitTorrentService) {
		this.bitTorrentService = bitTorrentService;
	}
	
	public void setShareService(IShareService shareService) {
		this.shareService = shareService;
	}

	public void setCompareService(ILocalFileCompareService compareService) {
		this.compareService = compareService;
	}

	public void setRequestService(IRequestService requestService) {
		this.requestService = requestService;
	}

	@Override
	public void run() {
		//every meta message we receive needs to be handled!!!
		//this is due to the fact that it could happen that two parallel trackers run!!!
		
		//todo: if not prepared don't do anything for the time being
		//think of this later
		//todo: we need also to do a isRequested check in case we are not the seeder but want the file!
		if(this.shareService.isPrepared(this.msg.getPieFile())) {
			this.bitTorrentService.shareFile(this.msg.getFileMeta());
			return;
		}
		
		if(!this.compareService.isConflictedOrNotNeeded(this.msg.getPieFile()) && this.requestService.handleRequest(this.msg.getPieFile(), true)) {
			this.bitTorrentService.handleFile(this.msg.getFileMeta());
			return;
		}
		
		PieLogger.debug(this.getClass(), "File {} not prepared or not needed!", this.msg.getPieFile().getFileName());
	}
	
}
