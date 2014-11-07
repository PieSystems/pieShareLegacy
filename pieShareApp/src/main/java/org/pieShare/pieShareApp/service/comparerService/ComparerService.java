/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pieShare.pieShareApp.service.comparerService;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import org.pieShare.pieShareApp.service.comparerService.api.IComparerService;
import org.pieShare.pieShareApp.service.comparerService.exceptions.FileConflictException;
import org.pieShare.pieShareApp.service.configurationService.api.IPieShareAppConfiguration;
import org.pieShare.pieShareApp.model.pieFile.PieFile;
import org.pieShare.pieShareApp.service.fileService.api.IFileUtilsService;
import org.pieShare.pieTools.pieUtilities.service.pieLogger.PieLogger;

/**
 *
 * @author Richard
 */
public class ComparerService implements IComparerService {

	private IPieShareAppConfiguration pieAppConfig;
	private IFileUtilsService fileUtilsService;

	public void setFileUtilsService(IFileUtilsService fileUtilsService) {
		this.fileUtilsService = fileUtilsService;
	}

	public void setPieShareConfiguration(IPieShareAppConfiguration pieAppConfig) {
		this.pieAppConfig = pieAppConfig;
	}

	@Override
	public int comparePieFile(PieFile pieFile) throws IOException, FileConflictException {
		
		PieLogger.debug(this.getClass(), "Comparing file: {}", pieFile.getRelativeFilePath());

		File localFile = new File(pieAppConfig.getWorkingDirectory(), pieFile.getRelativeFilePath());
		
		if (!localFile.exists()) {
			PieLogger.debug(this.getClass(), "{} does not exist. Request this file.", pieFile.getRelativeFilePath());
			
			//todo: a history check has to be done here to check for deleted files
			
			return 1;
		}
		
		PieFile localPieFile = this.fileUtilsService.getPieFile(localFile);


		//Remote File is older than local file
		//todo: should compare also file name!!!
		if (pieFile.getLastModified() == localFile.lastModified()) {
			if (Arrays.equals(pieFile.getMd5(), localPieFile.getMd5())) {
				PieLogger.debug(this.getClass(), "{} is already there. Do not request.", pieFile.getRelativeFilePath());
				return 0;
			}
			//todo: fix this exception: why does it take a PieFile?!
			throw new FileConflictException(String.format("Same Modification Date but different MD5 sum: %s", pieFile.getRelativeFilePath()), pieFile);
		} //Remote File is older than local file
		else if (pieFile.getLastModified() < localFile.lastModified()) {
			return -1;
		} //Remote File is newer than local file
		else if (pieFile.getLastModified() > localFile.lastModified()) {
			return 1;
		}

		throw new FileConflictException("Cannot handle this file.", pieFile);
	}

}
