package org.pieShare.pieTools.piePlate.service.cluster.api;

import java.util.List;
import org.pieShare.pieTools.piePlate.model.message.api.IClusterMessage;
import org.pieShare.pieTools.piePlate.service.channel.api.IIncomingChannel;
import org.pieShare.pieTools.piePlate.service.channel.api.IOutgoingChannel;
import org.pieShare.pieTools.piePlate.service.cluster.event.ClusterRemovedEvent;
import org.pieShare.pieTools.piePlate.service.cluster.event.IClusterRemovedListener;
import org.pieShare.pieTools.piePlate.service.cluster.exception.ClusterServiceException;
import org.pieShare.pieTools.pieUtilities.service.eventBase.IEventBase;

/**
 * Created by vauvenal5 on 12/12/13.
 */
public interface IClusterService {

	void connect(String clusterName) throws ClusterServiceException;
	
	String getClustername();
	
	void setClustername(String id);
	
	void reconnect() throws ClusterServiceException;
	
	void disconnect() throws ClusterServiceException;

	void sendMessage(IClusterMessage msg) throws ClusterServiceException;

	boolean isConnectedToCluster();
	
	IEventBase<IClusterRemovedListener, ClusterRemovedEvent> getClusterRemovedEventBase();
	
	void registerIncomingChannel(IIncomingChannel channel);
	
	void registerOutgoingChannel(IOutgoingChannel channel);
	
	@Deprecated
	List<IIncomingChannel> getIncomingChannels();
	
	boolean isMaster();
}
