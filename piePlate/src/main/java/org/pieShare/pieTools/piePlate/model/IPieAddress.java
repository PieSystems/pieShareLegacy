/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pieShare.pieTools.piePlate.model;

/**
 *
 * @author Svetoslav
 */
public interface IPieAddress {

	String getClusterName();

	void setClusterName(String cluster);
	
	String getChannelId();
	
	void setChannelId(String id);
}
