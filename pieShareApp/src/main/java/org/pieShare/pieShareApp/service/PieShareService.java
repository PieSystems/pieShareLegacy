/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pieShare.pieShareApp.service;

import java.util.ArrayList;
import java.util.List;
import org.pieShare.pieShareApp.model.PieUser;
import org.pieShare.pieShareApp.service.configurationService.api.IConfigurationFactory;
import org.pieShare.pieShareApp.service.database.api.IDatabaseService;
import org.pieShare.pieShareApp.service.userService.IUserService;
import org.pieShare.pieTools.piePlate.service.cluster.api.IClusterManagementService;
import org.pieShare.pieTools.pieUtilities.service.pieExecutorService.PieExecutorTaskFactory;
import org.pieShare.pieTools.pieUtilities.service.shutDownService.api.IShutdownService;

/**
 *
 * @author Svetoslav
 */
public class PieShareService {

	private PieExecutorTaskFactory executorFactory;
	private IClusterManagementService clusterManagementService;
	private IShutdownService shutdownService;
	private IDatabaseService databaseService;
	private IConfigurationFactory configurationFactory;
	private IUserService userService;
	
	public void setUserService(IUserService userService) {
		this.userService = userService;
	}

	public void setDatabaseService(IDatabaseService databaseService) {
		this.databaseService = databaseService;
	}

	public void setShutdownService(IShutdownService shutdownService) {
		this.shutdownService = shutdownService;
	}

	public void setExecutorFactory(PieExecutorTaskFactory executorFactory) {
		this.executorFactory = executorFactory;
	}

	public void setClusterManagementService(IClusterManagementService service) {
		this.clusterManagementService = service;
	}

	public void setConfigurationFactory(IConfigurationFactory configurationFactory) {
		this.configurationFactory = configurationFactory;
	}

	public void start() {
		//this.executorService.registerTask(SimpleMessage.class, PrintEventTask.class);

		/*
		 //unimportant for the time being because we don't have commandline support
		 try {
		 //todo-sv: change this!!! (new should not be used here)
		 //getbean per class ist dumm... zerst?rt unabh?ngigkeit
		 //SimpleMessageActionService action = this.beanService.getBean(SimpleMessageActionService.class);
		 //this.parserService.registerAction(action);
		 LoginActionService laction = this.beanService.getBean(PieShareAppBeanNames.getLoginActionServiceName());
		 this.parserService.registerAction(laction);
		 } catch (Exception ex) {
		 ex.printStackTrace();
		 }*/
		PieUser user = null;
		List<PieUser> users = databaseService.findAllPieUser();
		if (users != null && users.size() > 0) {
			user = users.get(0);
		}

		if (user == null) {
			user = userService.getUser();
		}
		
		user.setPieShareConfiguration(configurationFactory.checkAndCreateConfig(user.getPieShareConfiguration(), false));
	}

	public void stop() {
		//not needed anymore because cluster now implements shutdownable
		/*try {
			this.clusterManagementService.diconnectAll();
		}
		catch (ClusterManagmentServiceException ex) {
			PieLogger.error(this.getClass(), "Stop all failed!", ex);
		}*/
		
		this.shutdownService.fireShutdown();
		//System.exit(0);
	}
}
