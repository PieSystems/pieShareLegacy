/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pieShare.pieTools.piePlate.service.loophole;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.inject.Provider;
import org.pieShare.pieTools.piePlate.model.message.loopHoleMessages.api.IUdpMessage;
import org.pieShare.pieTools.piePlate.service.loophole.api.ILoopHoleFactory;
import org.pieShare.pieTools.piePlate.service.loophole.api.ILoopHoleService;
import org.pieShare.pieTools.piePlate.service.loophole.event.NewLoopHoleConnectionEvent;
import org.pieShare.pieTools.piePlate.service.loophole.event.api.INewLoopHoleConnectionEventListener;
import org.pieShare.pieTools.piePlate.service.serializer.api.ISerializerService;
import org.pieShare.pieTools.piePlate.service.serializer.exception.SerializerServiceException;
import org.pieShare.pieTools.pieUtilities.service.eventBase.IEventBase;
import org.pieShare.pieTools.pieUtilities.service.idService.api.IIDService;
import org.pieShare.pieTools.pieUtilities.service.networkService.api.IUdpPortService;
import org.pieShare.pieTools.pieUtilities.service.pieExecutorService.PieExecutorService;
import org.pieShare.pieTools.pieUtilities.service.pieExecutorService.PieExecutorTaskFactory;
import org.pieShare.pieTools.pieUtilities.service.pieLogger.PieLogger;

/**
 *
 * @author RicLeo00
 */
public class LoopHoleFactory implements ILoopHoleFactory {

    private final HashMap<String, ILoopHoleService> loopQueue;
    private IUdpPortService udpPortService;
    private int nextUdpPort;
    private String clientID;
    private IIDService idService;
    private ISerializerService serializerService;
    private final InetSocketAddress serverAddress;
    private String name;
    private PieExecutorTaskFactory executorFactory;
    private PieExecutorService executorService;
	private Provider<ILoopHoleService> loopHoleServiceProvider;

    private IEventBase<INewLoopHoleConnectionEventListener, NewLoopHoleConnectionEvent> newLoopHoleConnectionEvent;
    private List<InetSocketAddress> members;
    private List<Integer> localUsedPorts;

    public LoopHoleFactory() {
        loopQueue = new HashMap<>();

        nextUdpPort = 1234;

        serverAddress = new InetSocketAddress("server.piesystems.org", 6312);
        localUsedPorts = new ArrayList<>();
        members = new ArrayList<>();
    }

    @PostConstruct
    public void init() {
        clientID = idService.getNewID();
    }

    public void setMembers(List<InetSocketAddress> members) {
        this.members = members;
    }

    @Override
    public void addLocalUsedPort(int port) {
        localUsedPorts.add(port);
    }

    @Override
    public IEventBase<INewLoopHoleConnectionEventListener, NewLoopHoleConnectionEvent> getNewLoopHoleConnectionEvent() {
        return newLoopHoleConnectionEvent;
    }

    public void setNewLoopHoleConnectionEvent(IEventBase<INewLoopHoleConnectionEventListener, NewLoopHoleConnectionEvent> newLoopHoleConnectionEvent) {
        this.newLoopHoleConnectionEvent = newLoopHoleConnectionEvent;
    }

    @Override
    public synchronized void newClientAvailable(InetSocketAddress address, DatagramSocket socket) {
        members.add(address);
        PieLogger.info(this.getClass(), String.format("New UPD connection available. Host: %s, Port: %s", address.getHostString(), address.getPort()));
        newLoopHoleConnectionEvent.fireEvent(new NewLoopHoleConnectionEvent(this, address, socket));
        initializeNewLoopHole();
    }

    @Override
    public String getClientID() {
        return clientID;
    }

    public void setExecutorFactory(PieExecutorTaskFactory executorFactory) {
        this.executorFactory = executorFactory;
    }

    public void setExecutorService(PieExecutorService executorService) {
        this.executorService = executorService;
    }

    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    public void setSerializerService(ISerializerService serializerService) {
        this.serializerService = serializerService;
    }

    public void setIdService(IIDService idService) {
        this.idService = idService;
    }

	public void setLoopHoleServiceProvider(Provider<ILoopHoleService> loopHoleServiceProvider) {
		this.loopHoleServiceProvider = loopHoleServiceProvider;
	}

    public void setUdpPortService(IUdpPortService udpPortService) {
        this.udpPortService = udpPortService;
    }

    @Override
    public void initializeNewLoopHole() {

        nextUdpPort = udpPortService.getNewPortFrom(nextUdpPort);

		//todo: why does not the loophole service itself get the next port?!
        int newPort = 0;
        while (newPort != nextUdpPort) {
            newPort = nextUdpPort;
            if (localUsedPorts.contains(newPort)) {
                nextUdpPort++;
            }
        }

		//todo: why is loopHoleService prototyped
		ILoopHoleService loopHoleService = this.loopHoleServiceProvider.get();
        loopHoleService.setLocalPort(nextUdpPort);
        loopHoleService.setName(name);

        loopHoleService.init();
    }

    @Override
    public void sendToServer(DatagramSocket socket, IUdpMessage msg) {
        send(socket, msg, serverAddress);
    }

    private synchronized void send(DatagramSocket socket, IUdpMessage msg, InetSocketAddress address) {
        try {
            msg.setSenderID(this.clientID);
            byte[] bytes = serializerService.serialize(msg);
            DatagramPacket packet = new DatagramPacket(bytes, bytes.length, address.getAddress(), address.getPort());
            socket.send(packet);

            Thread.sleep(500);
        } catch (SerializerServiceException ex) {
            PieLogger.error(this.getClass(), "Error serializing message", ex);
        } catch (UnknownHostException ex) {
            PieLogger.error(this.getClass(), "UnknownHostException", ex);
        } catch (IOException ex) {
            PieLogger.error(this.getClass(), "IOException", ex);
        } catch (InterruptedException ex) {
            PieLogger.error(this.getClass(), "InterruptedException", ex);
        }
    }

    @Override
    public ILoopHoleService getLoopHoleService(String clientID) {
        return loopQueue.get(clientID);
    }

    @Override
    public void insertLoopHoleService(String ID, ILoopHoleService service) {
        loopQueue.put(ID, service);
    }
}
