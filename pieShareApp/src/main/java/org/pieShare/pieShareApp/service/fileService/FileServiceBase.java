/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pieShare.pieShareApp.service.fileService;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.pieShare.pieShareApp.model.PieShareAppBeanNames;
import org.pieShare.pieShareApp.model.PieUser;
import org.pieShare.pieShareApp.model.message.FileListRequestMessage;
import org.pieShare.pieShareApp.model.pieFile.PieFile;
import org.pieShare.pieShareApp.service.configurationService.api.IPieShareConfiguration;
import org.pieShare.pieShareApp.service.fileService.fileListenerService.api.IFileWatcherService;
import org.pieShare.pieShareApp.service.fileService.api.IFileService;
import org.pieShare.pieTools.piePlate.service.cluster.api.IClusterManagementService;
import org.pieShare.pieTools.piePlate.service.cluster.event.ClusterAddedEvent;
import org.pieShare.pieTools.piePlate.service.cluster.event.IClusterAddedListener;
import org.pieShare.pieTools.piePlate.service.cluster.exception.ClusterManagmentServiceException;
import org.pieShare.pieTools.pieUtilities.service.beanService.IBeanService;
import org.pieShare.pieTools.pieUtilities.service.pieExecutorService.api.IExecutorService;
import org.pieShare.pieTools.pieUtilities.service.pieLogger.PieLogger;

/**
 *
 * @author Svetoslav
 */
public abstract class FileServiceBase implements IFileService {
	
	protected IBeanService beanService;
	protected IPieShareConfiguration configuration;
	protected IFileWatcherService fileWatcherService;

	public void setBeanService(IBeanService beanService) {
		this.beanService = beanService;
	}

	public void setFileWatcherService(IFileWatcherService fileWatcherService) {
		this.fileWatcherService = fileWatcherService;
	}
	
	@Override
	public void waitUntilCopyFinished(File file) {
		FileInputStream st;
		boolean isCopying = true;

		while (isCopying) {

			try {
				Thread.sleep(2000);
				st = new FileInputStream(file);
				isCopying = false;
				st.close();
			}
			catch (FileNotFoundException ex) {
				isCopying = false;
			}
			catch (IOException ex) {
				//nothing needed to do here
			}
			catch (InterruptedException ex) {
				//nothing needed to do here
			}
		}
	}
	
	@Override
	public void deleteRecursive(PieFile file) {
		File localFile = new File(this.configuration.getWorkingDir(), file.getRelativeFilePath());
		try {
			if (localFile.isDirectory()) {
				FileUtils.deleteDirectory(localFile);
			}
			else {
				localFile.delete();
			}
		}
		catch (IOException ex) {
			PieLogger.error(this.getClass(), "Deleting failed!", ex);
		}
	}
	
	@Override
	public void setCorrectModificationDate(PieFile file) {
		PieLogger.trace(this.getClass(), "Date modified {} of {}", file.getLastModified(), file.getRelativeFilePath());
		File targetFile = new File(this.configuration.getWorkingDir(), file.getRelativeFilePath());

		this.fileWatcherService.addPieFileToModifiedList(file);
		if (!targetFile.setLastModified(file.getLastModified())) {
			this.fileWatcherService.removePieFileFromModifiedList(file);
			PieLogger.warn(this.getClass(), "Could not set LastModificationDate: {}", file.getRelativeFilePath());
		}
	}
	
	@Override
	public Path relitivizeFilePath(File file) {
		Path pathBase = configuration.getWorkingDir().getAbsoluteFile().toPath();//new File(pieAppConfig.getWorkingDirectory().getAbsolutePath()).toPath();
		Path pathAbsolute = file.getAbsoluteFile().toPath(); // Paths.get("/var/data/stuff/xyz.dat");
		return pathBase.relativize(pathAbsolute);
	}
}
