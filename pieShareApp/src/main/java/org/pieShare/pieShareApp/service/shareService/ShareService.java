/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pieShare.pieShareApp.service.shareService;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.ConcurrentHashMap;
import org.pieShare.pieShareApp.model.pieFilder.PieFile;
import org.pieShare.pieShareApp.service.fileService.api.IFileService;
import org.pieShare.pieShareApp.service.fileService.api.IFileWatcherService;
import org.pieShare.pieShareApp.service.fileService.fileEncryptionService.IFileEncryptionService;
import org.pieShare.pieTools.pieUtilities.service.pieLogger.PieLogger;
import org.pieShare.pieShareApp.service.comparerService.api.ICompareService;

/**
 *
 * @author Svetoslav
 */
public class ShareService implements IShareService{
	
	private ICompareService comparerService;
	private IFileService fileService;
	private IFileEncryptionService fileEncryptionService;
	private IFileWatcherService fileWatcherService;
	
	private ConcurrentHashMap<PieFile, Boolean> preparedFiles;

	public void init() {
		this.preparedFiles = new ConcurrentHashMap<>();
	}

	public void setFileEncryptionService(IFileEncryptionService fileEncryptionService) {
		this.fileEncryptionService = fileEncryptionService;
	}

	public void setFileWatcherService(IFileWatcherService fileWatcherService) {
		this.fileWatcherService = fileWatcherService;
	}

	public void setFileService(IFileService fileService) {
		this.fileService = fileService;
	}

	public void setComparerService(ICompareService comparerService) {
		this.comparerService = comparerService;
	}
	
	@Override
	public File prepareFile(PieFile file) throws NoLocalFileException
	{		
		if(!this.comparerService.equalsWithLocalPieFile(file)) {
			throw new NoLocalFileException("Files don't match!");
		}
		
		File localFile = this.fileService.getAbsolutePath(file);
		File localTmpFileParent = this.fileService.getAbsoluteTmpPath(file).getParentFile();
		File localEncTmpFile = new File(localTmpFileParent, file.getName()+".enc");
		
		synchronized(file) {
			if(this.isPrepared(file)) {			
				return localEncTmpFile;
			}

			//todo: this exception belongs into the encryption service!!!
			/*if(!localFile.exists()) {
				throw new NoLocalFileException("Local file doesn't exist!");
			}*/
			//TODO: create dirs???!!!
			this.fileEncryptionService.encryptFile(localFile, localEncTmpFile, false);
			this.preparedFiles.put(file, Boolean.TRUE);
		}
		
		return localEncTmpFile;
	}

	@Override
	public void localFileTransferComplete(PieFile file, boolean source) {
//		try {
			if(!source) {
				File localTmpFile = this.fileService.getAbsoluteTmpPath(file);
				File localEncTmpFile = new File(localTmpFile.getParentFile(), file.getName()+".enc");
				File localFile = this.fileService.getAbsolutePath(file);
				
				//todo: does this belong into the fileService?
				if (!localFile.getParentFile().exists()) {
					localFile.getParentFile().mkdirs();
				}
				
				//todo-android: do the decryption directly into the working dir
					//further there is a check nesseccary if there is a conflict 
					//between the recived file and the one in the working dir
				//this.fileEncryptionService.decryptFile(localEncTmpFile, localTmpFile, false);
				this.fileWatcherService.addPieFileToModifiedList(file);
				this.fileEncryptionService.decryptFile(localEncTmpFile, localFile, false);
				
				//this.fileService.setCorrectModificationDateOnTmpFile(file);
				//todo: check if 2nd modified event is thrown!!
				this.fileService.setCorrectModificationDate(file);

				this.fileService.deleteRecursive(localEncTmpFile);
				
				//this.fileWatcherService.addPieFileToModifiedList(file);
				//todo-android: this needs to be removed because android does not support this class
				//Files.move(localTmpFile.toPath(), localFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
				
				//todo: is it better to delete the enc file or not?
			}
//		} catch (IOException ex) {
//			PieLogger.error(this.getClass(), "Error!", ex);
//		}
	}
	
	@Override
	public void revokePrepared(PieFile file) {
		synchronized(file) {
			this.preparedFiles.remove(file);
		}
	}
	
	@Override
	public boolean isPrepared(PieFile file) {
		File localTmpFileParent = this.fileService.getAbsoluteTmpPath(file).getParentFile();
		File localEncTmpFile = new File(localTmpFileParent, file.getName()+".enc");
		synchronized(file) {
                        if(this.preparedFiles.get(file) == null) {
                            return Boolean.FALSE && localEncTmpFile.exists();
                        }
			return this.preparedFiles.get(file) && localEncTmpFile.exists();
		}
	}
}
