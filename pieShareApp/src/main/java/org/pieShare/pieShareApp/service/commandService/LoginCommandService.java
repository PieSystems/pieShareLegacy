/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pieShare.pieShareApp.service.commandService;

import org.pieShare.pieShareApp.model.PieShareAppBeanNames;
import org.pieShare.pieShareApp.model.PieUser;
import org.pieShare.pieShareApp.model.command.LoginCommand;
import org.pieShare.pieTools.pieUtilities.model.EncryptedPassword;
import org.pieShare.pieTools.pieUtilities.service.beanService.IBeanService;
import org.pieShare.pieTools.pieUtilities.service.commandService.api.ICommandService;
import org.pieShare.pieTools.pieUtilities.service.security.pbe.IPasswordEncryptionService;

/**
 *
 * @author Svetoslav
 */
public class LoginCommandService implements ICommandService<LoginCommand> {

    private IPasswordEncryptionService passwordEncryptionService;
    private IBeanService beanService;

    public void setBeanService(IBeanService beanService) {
        this.beanService = beanService;
    }
    
    public void setPasswordEncryptionService(IPasswordEncryptionService service) {
        this.passwordEncryptionService = service;
    }
    
    @Override
    public void executeCommand(LoginCommand command) {
        EncryptedPassword pwd = this.passwordEncryptionService.encryptPassword(command.getPlainTextPassword());
        
        PieUser user = (PieUser)this.beanService.getBean(PieShareAppBeanNames.getPieUser());
        user.setPassword(pwd);
        user.setUserName(command.getUserName());
        user.setIsLoggedIn(true);
        
        this.beanService.getBean(PieShareAppBeanNames.getFileServiceName());
    }
    
}
