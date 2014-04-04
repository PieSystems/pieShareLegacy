/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pieShare.pieTools.pieUtilities.service.cmdLineService;

import org.pieShare.pieTools.pieUtilities.service.cmdLineService.api.ICmdLineService;
import org.pieShare.pieTools.pieUtilities.service.pieExecutorService.api.IPieTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 *
 * @author Svetoslav
 */
@Component
public class ReadLineTask implements IPieTask {
    
    private ICmdLineService cmdService;
    
    @Autowired
    @Qualifier("cmdLineService")
    public void setCmdLineService(ICmdLineService service) {
        this.cmdService = service;
    }
    
    @Override
    public void run() {
        this.cmdService.readCommand();
    }
    
}
