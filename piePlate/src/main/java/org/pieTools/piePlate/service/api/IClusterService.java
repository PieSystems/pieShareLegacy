package org.pieTools.piePlate.service.api;

import org.jgroups.JChannel;
import org.jgroups.Message;
import org.pieTools.piePlate.service.exception.ClusterServiceException;

/**
 * Created by vauvenal5 on 12/12/13.
 */
public interface IClusterService {
    void connect(String clusterName) throws ClusterServiceException;

    void handleMessage(Message msg);
}
