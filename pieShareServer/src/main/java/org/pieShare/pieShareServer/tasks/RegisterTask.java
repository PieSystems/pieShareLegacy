/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pieShare.pieShareServer.tasks;

import java.util.HashMap;
import org.pieShare.pieTools.piePlate.model.UdpAddress;
import org.pieShare.pieTools.piePlate.model.message.loopHoleMessages.LoopHoleConnectionMessage;
import org.pieShare.pieTools.piePlate.model.message.loopHoleMessages.RegisterMessage;
import org.pieShare.pieTools.pieUtilities.service.pieExecutorService.api.task.IPieEventTask;
import org.pieShare.pieShareServer.services.loopHoleService.api.ILoopHoleService;
import org.pieShare.pieShareServer.services.loopHoleService.api.IUserPersistanceService;
import org.pieShare.pieShareServer.services.model.User;
import org.pieShare.pieTools.pieUtilities.service.pieLogger.PieLogger;

/**
 *
 * @author Richard
 */
public class RegisterTask implements IPieEventTask<RegisterMessage> {

    private RegisterMessage msg;
    private IUserPersistanceService userPersistanceService;
    private ILoopHoleService loopHoleService;

    public void setLoopHoleService(ILoopHoleService loopHoleService) {
        this.loopHoleService = loopHoleService;
    }

    public void setUserPersistanceService(IUserPersistanceService userPersistanceService) {
        this.userPersistanceService = userPersistanceService;
    }

    @Override
    public void setEvent(RegisterMessage msg) {
        this.msg = msg;
    }

    @Override
    public void run() {

        User newUser = new User();
        newUser.setId(msg.getSenderID());
        newUser.setName(msg.getName());

        UdpAddress privateAddress = new UdpAddress();
        privateAddress.setHost(msg.getPrivateHost());
        privateAddress.setPort(msg.getPrivatePort());

        newUser.setPrivateAddress(privateAddress);
        newUser.setPublicAddress(msg.getSenderAddress());
        newUser.setLoopHoleID(msg.getLocalLoopID());

        PieLogger.info(this.getClass(), String.format("User:        %s  with SubUD: %s   registered sucessfully.", newUser.getName(), msg.getLocalLoopID()));
        PieLogger.info(this.getClass(), String.format("PublicHost:  %s, PublicPort:  %s", newUser.getPublicAddress().getHost(), newUser.getPublicAddress().getPort()));
        PieLogger.info(this.getClass(), String.format("PrivateHost: %s, PrivatePort: %s", newUser.getPrivateAddress().getHost(), newUser.getPrivateAddress().getPort()));

        LoopHoleConnectionMessage connectionMessageToReceiver = new LoopHoleConnectionMessage();
        connectionMessageToReceiver.setClientPrivateIP(newUser.getPrivateAddress().getHost());
        connectionMessageToReceiver.setClientPrivatePort(newUser.getPrivateAddress().getPort());
        connectionMessageToReceiver.setClientPublicIP(newUser.getPublicAddress().getHost());
        connectionMessageToReceiver.setClientPublicPort(newUser.getPublicAddress().getPort());
        connectionMessageToReceiver.setFromId(newUser.getId());

        HashMap<String, User> notConnectedUsers = userPersistanceService.getNonConnectedUsersByName(msg.getName());
        for (User user : notConnectedUsers.values()) {
            PieLogger.info(this.getClass(), String.format("Found non connected users. Count: %s", notConnectedUsers.size()));
            if (!msg.getSenderID().equals(user.getId())) {

                LoopHoleConnectionMessage connectionMessageToSender = new LoopHoleConnectionMessage();
                connectionMessageToSender.setClientPrivateIP(user.getPrivateAddress().getHost());
                connectionMessageToSender.setClientPrivatePort(user.getPrivateAddress().getPort());
                connectionMessageToSender.setClientPublicIP(user.getPublicAddress().getHost());
                connectionMessageToSender.setClientPublicPort(user.getPublicAddress().getPort());
                connectionMessageToSender.setFromId(user.getId());
                connectionMessageToSender.setSenderID(msg.getSenderID());
                connectionMessageToSender.setLocalLoopID(msg.getLocalLoopID());
                connectionMessageToSender.setClientLocalLoopID(user.getLoopHoleID());

                loopHoleService.send(connectionMessageToSender, msg.getSenderAddress());

                connectionMessageToReceiver.setSenderID(user.getId());
                connectionMessageToReceiver.setLocalLoopID(user.getLoopHoleID());
                connectionMessageToReceiver.setClientLocalLoopID(msg.getLocalLoopID());

                loopHoleService.send(connectionMessageToReceiver, user.getPublicAddress());

                user.setConnectedTo(msg.getLocalLoopID());
                userPersistanceService.mergeUser(user);
                newUser.setConnectedTo(user.getLoopHoleID());
                break;
            }
        }

        userPersistanceService.addUser(newUser);

        /*
         for (User user : notConnectedUsers.values()) {

         if (!msg.getSenderID().equals(user.getId())) {

         LoopHoleConnectionMessage connectionMessageToSender = new LoopHoleConnectionMessage();
         connectionMessageToSender.setClientPrivateIP(user.getPrivateAddress().getHost());
         connectionMessageToSender.setClientPrivatePort(user.getPrivateAddress().getPort());
         connectionMessageToSender.setClientPublicIP(user.getPublicAddress().getHost());
         connectionMessageToSender.setClientPublicPort(user.getPublicAddress().getPort());
         connectionMessageToSender.setFromId(user.getId());
         connectionMessageToSender.setSenderID(msg.getSenderID());
         loopHoleService.send(connectionMessageToSender, msg.getSenderAddress());
         }
         connectionMessageToReceiver.setSenderID(user.getId());
         loopHoleService.send(connectionMessageToReceiver, user.getPublicAddress());
         }*/
    }
}
