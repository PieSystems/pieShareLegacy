/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pieShare.pieShareApp.model.message;

import org.pieShare.pieShareApp.service.fileService.PieFile;
import org.pieShare.pieTools.piePlate.model.message.HeaderMessage;

/**
 *
 * @author Svetoslav
 */
public class FileDeletedMessage extends HeaderMessage {
	private PieFile file;

	public PieFile getFile() {
		return file;
	}

	public void setFile(PieFile file) {
		this.file = file;
	}
	
	public FileDeletedMessage() {
	}
}
