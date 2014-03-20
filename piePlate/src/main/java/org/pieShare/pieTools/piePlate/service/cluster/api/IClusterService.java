package org.pieShare.pieTools.piePlate.service.cluster.api;

import org.pieShare.pieTools.piePlate.model.message.api.IPieMessage;
import org.pieShare.pieTools.piePlate.model.task.api.IMessageTask;
import org.pieShare.pieTools.piePlate.service.cluster.exception.ClusterServiceException;

/**
 * Created by vauvenal5 on 12/12/13.
 */
public interface IClusterService {
    <P extends IPieMessage> void registerTask(Class<P> clazz, IMessageTask<P> task);

    void connect(String clusterName) throws ClusterServiceException;

    void sendMessage(IPieMessage msg) throws ClusterServiceException;

    int getMembersCount();

    boolean isConnectedToCluster();
}