package org.pieShare.pieTools.piePlate.service.cluster.api;

import org.pieShare.pieTools.piePlate.model.task.api.IMessageTask;
import org.pieShare.pieTools.piePlate.model.message.api.IPieMessage;

/**
 * Created by Svetoslav on 14.01.14.
 */
public interface IClusterMessageHandler {
    <P extends IPieMessage> void registerTask(Class<P> clazz, IMessageTask<P> task);

    void handleMessage(IPieMessage msg);
}
