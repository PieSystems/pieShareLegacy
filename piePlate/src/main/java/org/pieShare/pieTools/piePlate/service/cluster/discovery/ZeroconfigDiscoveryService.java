/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pieShare.pieTools.piePlate.service.cluster.discovery;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.inject.Provider;
import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;
import javax.jmdns.ServiceListener;
import org.pieShare.pieTools.piePlate.model.DiscoveredMember;
import org.pieShare.pieTools.piePlate.service.cluster.discovery.event.IMemberDiscoveredListener;
import org.pieShare.pieTools.pieUtilities.service.networkService.INetworkService;
import org.pieShare.pieTools.pieUtilities.service.pieLogger.PieLogger;
import org.pieShare.pieTools.pieUtilities.service.shutDownService.api.AShutdownableService;

/**
 *
 * @author Svetoslav Videnov <s.videnov@dsg.tuwien.ac.at>
 */
public class ZeroconfigDiscoveryService extends AShutdownableService implements IDiscoveryService {

	private JmDNS jmDns;
	private INetworkService networkService;
	private String type = "_pieShare._pie.local.";
	private ServiceInfo myself;
	private Provider<DiscoveredMember> discoveredMemberProvider;
	private String cloudName;

	private IJmdnsDiscoveryListener listener;

	public ZeroconfigDiscoveryService() {
	}

	public void setDiscoveredMemberProvider(Provider<DiscoveredMember> discoveredMemberProvider) {
		this.discoveredMemberProvider = discoveredMemberProvider;
	}

	public void setNetworkService(INetworkService networkService) {
		this.networkService = networkService;
	}

	public void setListener(IJmdnsDiscoveryListener listener) {
		this.listener = listener;
	}

	private synchronized void initJmdns() throws DiscoveryException {
		if (this.jmDns != null) {
			return;
		}

		try {
			this.jmDns = JmDNS.create(this.networkService.getLocalHost(),
					this.networkService.getHostname());
		} catch (IOException ex) {
			throw new DiscoveryException("Init of jmdns failed!", ex);
		}
	}

	@Override
	public void registerService(String clusterName, int port) throws DiscoveryException {
		try {
			this.cloudName = clusterName;
			this.initJmdns();
			String me = String.format("%s.%s", clusterName, UUID.randomUUID().toString());
			this.type = String.format("_%s._pie.local.", clusterName.replace('.', '-'));
			PieLogger.trace(this.getClass(), "Registering myself with id {}", me);
			PieLogger.trace(this.getClass(), "Registering myself with type {}", this.type);
			this.myself = ServiceInfo.create(this.type, me, port, "");
			this.jmDns.registerService(this.myself);
			
			//todo-sv: resolve circular dependecy
			listener.setDiscoveryService(this);
			listener.setMyself(me);
			listener.setCloudName(clusterName);
			this.jmDns.addServiceListener(this.type, listener);
		} catch (IOException ex) {
			PieLogger.error(this.getClass(), "Could not create zeroconfig discovery.", ex);
			throw new DiscoveryException("Could not register service!", ex);
		}
	}
	
	public ServiceInfo resolveService(ServiceInfo info) {
		return this.jmDns.getServiceInfo(info.getType(), info.getName());
	}

	@Override
	public List<DiscoveredMember> list() throws DiscoveryException {
		//todo: think about changing this to a notify structure?
			//instead of returning a list just throw events for every
			//discovered member to all listeners?
		if(this.myself == null) {
			throw new DiscoveryException("Jmdns not yet registered!!!");
		}
		
		ServiceInfo[] list = this.jmDns.list(this.type);
		List<DiscoveredMember> members = new ArrayList<DiscoveredMember>();

		for (ServiceInfo info : list) {
			if (!info.getName().equals(this.myself.getName())
					&& info.getName().startsWith(this.cloudName)) {
				//todo-discovery: it could lead to problems if it retunrs multiple inetAdresses
				for (InetAddress ad : info.getInetAddresses()) {
					DiscoveredMember member = discoveredMemberProvider.get();
					member.setInetAdresses(ad);
					member.setPort(info.getPort());
					member.setName(info.getName());
					members.add(member);
				}
			}
		}

		PieLogger.trace(this.getClass(), "We discovered {} members!", members.size());

		return members;
	}

	@Override
	public void shutdown() {
		if (jmDns != null) {
			this.jmDns.unregisterAllServices();
			this.jmDns.removeServiceListener(this.type, listener);
			try {
				this.jmDns.close();
				this.jmDns = null;
			} catch (IOException ex) {
				PieLogger.error(this.getClass(), "Could not close jmdns.", ex);
				//fail silently due to shutdown
			}
		}
	}

	@Override
	public void addMemberDiscoveredListener(IMemberDiscoveredListener listener) {
		this.listener.getMemberDiscoveredEventBase().addEventListener(listener);
	}

	@Override
	public void removeMemberDiscoveredListener(IMemberDiscoveredListener listener) {
		this.listener.getMemberDiscoveredEventBase().removeEventListener(listener);
	}
}
