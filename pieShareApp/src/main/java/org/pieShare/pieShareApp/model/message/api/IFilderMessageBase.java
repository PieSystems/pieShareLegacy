/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pieShare.pieShareApp.model.message.api;

import org.pieShare.pieShareApp.model.pieFilder.PieFilder;
import org.pieShare.pieTools.piePlate.model.message.api.IEncryptedMessage;

/**
 * Interface for File and Folder Messages
 * T defines whether a file or a folder is expected (File, Folder extends Filder)
 * @author daniela
 * @param <T> defines whether a file or folder is in the message
 */
public interface IFilderMessageBase<T extends PieFilder> extends IEncryptedMessage{
        /**
         * Get a PieFilder from a Message
         * @return a PieFile or -Folder message, depending on the param <T>
         */
        T getPieFilder();

        /**
         * Set the PieFilder into the message
         * @param filder a PieFile or -Folder message, depending on the param <T>
         */
	void setPieFilder(T filder);
}
