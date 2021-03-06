/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pieShare.pieShareApp.task;

import org.pieShare.pieShareApp.model.PieUser;
import org.pieShare.pieShareApp.service.factoryService.IMessageFactoryService;
import org.pieShare.pieShareApp.service.userService.IUserService;
import org.pieShare.pieTools.piePlate.model.message.api.IClusterMessage;
import org.pieShare.pieTools.piePlate.service.cluster.api.IClusterManagementService;
import org.pieShare.pieTools.pieUtilities.service.pieExecutorService.api.task.IPieTask;

/**
 *
 * @author Svetoslav
 */
public abstract class AMessageSendingTask implements IPieTask {
	protected IClusterManagementService clusterManagementService;
	protected IMessageFactoryService messageFactoryService;
	protected IUserService userService;

	public void setClusterManagementService(IClusterManagementService clusterManagementService) {
		this.clusterManagementService = clusterManagementService;
	}

	public void setMessageFactoryService(IMessageFactoryService messageFactoryService) {
		this.messageFactoryService = messageFactoryService;
	}
	
	public void setUserService(IUserService userService) {
		this.userService = userService;
	}
	
	protected void setDefaultAdresse(IClusterMessage msg) {
		//todo: need somewhere a match between working dir and belonging cloud
		PieUser user = userService.getUser();
		msg.getAddress().setChannelId(user.getUserName());
		msg.getAddress().setClusterName(user.getCloudName());
	}
}
