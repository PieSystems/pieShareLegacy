/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pieShare.pieTools.piePlate.service.cluster;

import java.util.Map;
import java.util.Map.Entry;
import javax.inject.Provider;
import org.pieShare.pieTools.piePlate.model.message.api.IClusterMessage;
import org.pieShare.pieTools.piePlate.service.channel.api.IIncomingChannel;
import org.pieShare.pieTools.piePlate.service.channel.api.IOutgoingChannel;
import org.pieShare.pieTools.piePlate.service.channel.api.ITwoWayChannel;
import org.pieShare.pieTools.piePlate.service.cluster.api.IClusterManagementService;
import org.pieShare.pieTools.piePlate.service.cluster.api.IClusterService;
import org.pieShare.pieTools.piePlate.service.cluster.event.ClusterAddedEvent;
import org.pieShare.pieTools.piePlate.service.cluster.event.ClusterRemovedEvent;
import org.pieShare.pieTools.piePlate.service.cluster.event.IClusterAddedListener;
import org.pieShare.pieTools.piePlate.service.cluster.event.IClusterRemovedListener;
import org.pieShare.pieTools.piePlate.service.cluster.exception.ClusterManagmentServiceException;
import org.pieShare.pieTools.piePlate.service.cluster.exception.ClusterServiceException;
import org.pieShare.pieTools.piePlate.service.loophole.api.ILoopHoleFactory;
import org.pieShare.pieTools.pieUtilities.service.eventBase.IEventBase;
import org.pieShare.pieTools.pieUtilities.service.pieLogger.PieLogger;

/**
 *
 * @author Svetoslav
 */
public class ClusterManagementService implements IClusterManagementService {

	private Map<String, IClusterService> clusters;
	private IEventBase<IClusterAddedListener, ClusterAddedEvent> clusterAddedEventBase;
	private IEventBase<IClusterRemovedListener, ClusterRemovedEvent> clusterRemovedEventBase;
	private ILoopHoleFactory loopHoleFactory;
	private Provider<IClusterService> clusterServiceProvider;

	public void setLoopHoleFactory(ILoopHoleFactory loopHoleFactory) {
		this.loopHoleFactory = loopHoleFactory;
	}

	@Override
	public IEventBase<IClusterAddedListener, ClusterAddedEvent> getClusterAddedEventBase() {
		return this.clusterAddedEventBase;
	}

	public void setClusterServiceProvider(Provider<IClusterService> clusterServiceProvider) {
		this.clusterServiceProvider = clusterServiceProvider;
	}

	public void setClusterAddedEventBase(IEventBase<IClusterAddedListener, ClusterAddedEvent> clusterAddedEventBase) {
		this.clusterAddedEventBase = clusterAddedEventBase;
	}

	@Override
	public IEventBase<IClusterRemovedListener, ClusterRemovedEvent> getClusterRemovedEventBase() {
		return this.clusterRemovedEventBase;
	}

	public void setClusterRemovedEventBase(IEventBase<IClusterRemovedListener, ClusterRemovedEvent> clusterRemovedEventBase) {
		this.clusterRemovedEventBase = clusterRemovedEventBase;
	}

	public void setMap(Map<String, IClusterService> map) {
		this.clusters = map;
	}

	private IClusterService connect(String id) throws ClusterManagmentServiceException {
		//todo: think about this if this is the right place for this!
		//todo: as already told in the past we have to abstract the loophole server for the integration tests!!!
		//due to not doing this everything went tits up when they kicked our server... thx for that!!!
		//loopHoleFactory.setName(id);
		//loopHoleFactory.initializeNewLoopHole();

		if (this.clusters.containsKey(id)) {
			return this.clusters.get(id);
		}

		try {
			IClusterService cluster = this.clusterServiceProvider.get();
			cluster.setClustername(id);
			cluster.connect(id);
			this.clusters.put(id, cluster);
			this.clusterAddedEventBase.fireEvent(new ClusterAddedEvent(this, cluster));

			//todo: check if event listener deregistration is handled properly
			//todo: further check if events are needed at all in future
			cluster.getClusterRemovedEventBase().addEventListener(new IClusterRemovedListener() {
				@Override
				public void handleObject(ClusterRemovedEvent event) {
					clusters.remove(((IClusterService) event.getSource()).getClustername());
					clusterRemovedEventBase.fireEvent(event);
				}
			});

			return cluster;
		} catch (ClusterServiceException ex) {
			//should never happen
			throw new ClusterManagmentServiceException(ex);
		}
	}

	@Override
	public void sendMessage(IClusterMessage message) throws ClusterManagmentServiceException {
		if (!this.clusters.containsKey(message.getAddress().getClusterName())) {
			throw new ClusterManagmentServiceException(String.format("Cloud name not found: %s", message.getAddress().getClusterName()));
		}

		try {
			this.clusters.get(message.getAddress().getClusterName()).sendMessage(message);
		} catch (ClusterServiceException ex) {
			throw new ClusterManagmentServiceException(ex);
		}
	}

	@Override
	public void registerChannel(String clusterId, IIncomingChannel channel) throws ClusterManagmentServiceException {
		IClusterService cluster = this.connect(clusterId);
		cluster.registerIncomingChannel(channel);
	}

	@Override
	public void registerChannel(String clusterId, IOutgoingChannel channel) throws ClusterManagmentServiceException {
		IClusterService cluster = this.connect(clusterId);
		cluster.registerOutgoingChannel(channel);
	}

	@Override
	public void registerChannel(String clusterId, ITwoWayChannel channel) throws ClusterManagmentServiceException {
		IClusterService cluster = this.connect(clusterId);
		cluster.registerIncomingChannel(channel);
		cluster.registerOutgoingChannel(channel);
	}
	
	@Override
	public void reconnectAll() throws ClusterManagmentServiceException {
		for (Entry<String, IClusterService> entry : this.clusters.entrySet()) {
			try {
				entry.getValue().reconnect();
			} catch (ClusterServiceException ex) {
				//todo: error handling
				PieLogger.error(this.getClass(), "Reconnect all failed!", ex);
			}
		}
	}

	@Override
	public void disconnect(String id) throws ClusterServiceException {
		if (this.clusters.containsKey(id)) {
			this.clusters.get(id).disconnect();
		}
	}

	@Override
	public void diconnectAll() throws ClusterManagmentServiceException {
		for (Entry<String, IClusterService> entry : this.clusters.entrySet()) {
			try {
				entry.getValue().disconnect();
			} catch (ClusterServiceException ex) {
				//todo: error handling
				PieLogger.error(this.getClass(), "Disconnect all failed!", ex);
			}
		}
	}

	@Override
	public Map<String, IClusterService> getClusters() {
		return this.clusters;
	}
}
