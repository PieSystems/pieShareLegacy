/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pieShare.pieShareClient.services;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import org.bouncycastle.util.Arrays;
import org.pieShare.pieShareClient.api.Callback;
import org.pieShare.pieTools.pieUtilities.service.pieLogger.PieLogger;

/**
 *
 * @author RicLeo00
 */
public class ClientTask implements Runnable{

    private DatagramSocket socket;
    private Callback callback;
    
    public DatagramSocket getSocket() {
        return socket;
    }

    public void setSocket(DatagramSocket socket) {
        this.socket = socket;
    }
    
    public void setCallback(Callback callback) {
        this.callback = callback;
    }
    
    public void run() {
        while(true)
        {
            byte[] bytes = new byte[1024];
            DatagramPacket packet = new DatagramPacket(bytes, bytes.length);
            
            try {
                socket.receive(packet);
            } catch (IOException ex) {
                bytes = Arrays.copyOfRange(bytes, 0, packet.getLength());
                JsonObject input = processInput(new String(bytes));
                
                if(input.getString("type").equals("connection"))
                {
                    JsonObject newClient = input.getJsonObject("client");
                    callback.Handle(newClient);
                }
                if(input.getString("type").equals("msg"))
                {
                    System.out.println("Message Arrived: " + input.getString("msg"));
                }
            }
        }
    }
    
     public JsonObject processInput(String input) {
        ByteArrayInputStream byteInStream = new ByteArrayInputStream(input.getBytes());
        JsonReader jsonReader = Json.createReader(byteInStream);
        JsonObject ob = jsonReader.readObject();
        PieLogger.info(this.getClass(), String.format("ConnectionText: %s", ob.toString()));
        return ob;
    }
    
}