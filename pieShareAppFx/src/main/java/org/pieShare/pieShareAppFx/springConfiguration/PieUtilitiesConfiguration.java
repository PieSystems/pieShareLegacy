/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pieShare.pieShareAppFx.springConfiguration;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import org.pieShare.pieTools.pieUtilities.service.base64Service.Base64Service;
import org.pieShare.pieTools.pieUtilities.service.compressor.Compressor;
import org.pieShare.pieTools.pieUtilities.service.propertiesReader.PropertiesReader;
import org.pieShare.pieTools.pieUtilities.service.eventBase.EventBase;
import org.pieShare.pieTools.pieUtilities.service.idService.SimpleUUIDService;
import org.pieShare.pieTools.pieUtilities.service.networkService.NetworkService;
import org.pieShare.pieTools.pieUtilities.service.networkService.UdpPortService;
import org.pieShare.pieTools.pieUtilities.service.pieExecutorService.PieExecutorService;
import org.pieShare.pieTools.pieUtilities.service.pieExecutorService.PieExecutorTaskFactory;
import org.pieShare.pieTools.pieUtilities.service.regexService.RegexService;
import org.pieShare.pieTools.pieUtilities.service.security.BouncyCastleProviderService;
import org.pieShare.pieTools.pieUtilities.service.security.encodeService.EncodeService;
import org.pieShare.pieTools.pieUtilities.service.security.hashService.MD5Service;
import org.pieShare.pieTools.pieUtilities.service.security.pbe.PasswordEncryptionService;
import org.pieShare.pieTools.pieUtilities.service.shutDownService.ShutdownService;
import org.pieShare.pieTools.pieUtilities.service.tempFolderService.TempFolderService;
import org.pieshare.piespring.service.beanService.BeanService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;

/**
 *
 * @author Svetoslav
 */
@Configuration
public class PieUtilitiesConfiguration {

    @Bean
    @Lazy
    public BeanService beanService() {
        return new BeanService();
    }

    @Bean
    @Lazy
    @Scope(value = "prototype")
    public Map javaMap() {
        return new HashMap();
    }

    @Bean
    @Lazy
    public PieExecutorService pieExecutorService() {
        PieExecutorService service = PieExecutorService.newCachedPieExecutorService();
        service.setExecutorFactory(this.pieExecutorTaskFactory());
		service.setShutdownService(this.shutdownService());
        return service;
    }

    @Bean
    @Lazy
    public PieExecutorTaskFactory pieExecutorTaskFactory() {
        PieExecutorTaskFactory factory = new PieExecutorTaskFactory();
        factory.setTasks(this.javaMap());
        return factory;
	}
	
	@Bean
	@Lazy
	public ShutdownService shutdownService() {
		ShutdownService service = new ShutdownService();
		return service;
    }

    @Bean
    @Lazy
    public Base64Service base64Service() {
        return new Base64Service();
    }

    @Bean
    @Lazy
    @Scope(value = "prototype")
    public Compressor compressor() {
        Compressor com = new Compressor();
        com.setBase64Service(base64Service());
        return com;
    }

    @Bean
    @Lazy
    public PropertiesReader configurationReader() {
        return new PropertiesReader();
    }

    @Bean
    @Lazy
    public BouncyCastleProviderService providerService() {
        return new BouncyCastleProviderService();
    }

    @Bean
    @Lazy
    public PasswordEncryptionService passwordEncryptionService() {
        PasswordEncryptionService service = new PasswordEncryptionService();
        service.setProviderService(this.providerService());
        return service;
    }

    @Bean
    @Lazy
    public TempFolderService tempFolderService() {
        return new TempFolderService();
    }

    @Bean
    @Lazy
    public MD5Service md5Service() {
        MD5Service service = new MD5Service();
        service.setProviderService(this.providerService());
        return service;
    }

    @Bean
    @Lazy
    @Scope(value = "prototype")
    public EventBase eventBase() {
        return new EventBase();
    }

    @Bean
    @Lazy
    @Scope(value = "prototype")
    public RegexService regexService() {
        return new RegexService();
    }

    @Bean
    @Lazy
    public EncodeService encodeService() {
        EncodeService service = new EncodeService();
        service.setPasswordEncryptionService(passwordEncryptionService());
        service.setProviderService(providerService());
        return service;
    }

    @Bean
    @Lazy
    public SimpleUUIDService idService() {
        SimpleUUIDService service = new SimpleUUIDService();
        return service;
    }

    @Bean
    @Lazy
    public UdpPortService udpPortService() {
        UdpPortService service = new UdpPortService();
        return service;
    }
	
	@Bean
	@Lazy
	public NetworkService networkService() {
		return new NetworkService();
	}
	
	@Bean
	@Lazy
	public Date date() {
		return new Date();
	}
}
