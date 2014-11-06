/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pieShare.pieShareApp.service.configurationService;

import java.io.File;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Properties;
import org.pieShare.pieShareApp.service.configurationService.api.IPieShareAppConfiguration;
import org.pieShare.pieShareApp.service.database.api.IDatabaseService;
import org.pieShare.pieTools.pieUtilities.service.propertiesReader.api.IPropertiesReader;
import org.pieShare.pieTools.pieUtilities.service.propertiesReader.exception.NoConfigFoundException;
import org.pieShare.pieTools.pieUtilities.service.pieLogger.PieLogger;
import org.pieShare.pieTools.pieUtilities.service.regexService.IRegexService;

/**
 *
 * @author richy
 */
public class PieShareAppConfiguration implements IPieShareAppConfiguration {

	private IPropertiesReader configurationReader;
	private final String HOME_DIR;
	private Properties conf;
	private File BASE_CONFIG_FOLDER;
	private File configFile;
	private File workingDir = null;
	private File tempDir = null;
	private IDatabaseService databaseService;
	private IRegexService regexService;
	private final HashMap<String, String> shortcuts;

	public PieShareAppConfiguration() {
		shortcuts = new HashMap<>();
	
		//ToDo: Config Folder is hard coded. Check if we could do this in an other way.
		HOME_DIR = System.getProperty("user.home");
		BASE_CONFIG_FOLDER = new File(String.format("%s/%s/%s", HOME_DIR, ".pieSystems", ".pieShare"));

		if (!BASE_CONFIG_FOLDER.exists() || !BASE_CONFIG_FOLDER.isDirectory()) {
			BASE_CONFIG_FOLDER.mkdirs();
		}
	}

	public void setRegexService(IRegexService regexService) {
		this.regexService = regexService;
	}

	public void setDatabaseServie(IDatabaseService databaseService) {
		this.databaseService = databaseService;
	}

	@Override
	public File getBaseConfigPath() {
		return this.BASE_CONFIG_FOLDER;
	}

	@Override
	public File getPasswordFile() {
		return new File(BASE_CONFIG_FOLDER, "pwd.pie");
	}

	public void setConfigurationReader(IPropertiesReader configurationReader) {
		this.configurationReader = configurationReader;
	}

	public void init() {

		if (configFile == null) {
			this.configFile = new File(BASE_CONFIG_FOLDER, "pieShare.properties");
		}

		if (!configFile.getParentFile().exists()) {
			configFile.getParentFile().mkdirs();
		}

		shortcuts.put("%BASE_CONFIG%", BASE_CONFIG_FOLDER.toPath().toString());
		
		try {
			//pieShare.properties
			conf = configurationReader.getConfig(configFile);

//			for (Entry<String, String> shortcut : shortcuts.entrySet()) {
//				for (Entry<Object, Object> prop : conf.entrySet()) {
//					regexService.setPattern(String.format(".*%s.*", shortcut.getKey()));
//					prop.setValue(regexService.replaceAll((String) prop.getValue(), shortcut.getValue()));
//				}
//			}

		}
		catch (NoConfigFoundException ex) {
			PieLogger.error(this.getClass(), "Cannot find pieShareAppConfig.", ex);
		}
	}

	public void setConfigPath(String folder) {
		BASE_CONFIG_FOLDER = new File(BASE_CONFIG_FOLDER, folder);
		if (!BASE_CONFIG_FOLDER.exists()) {
			BASE_CONFIG_FOLDER.mkdirs();
		}
	}

	@Override
	public File getWorkingDirectory() {
		readWorkingDir();
		return workingDir;
	}

	@Override
	public void setWorkingDir(File workingDir) {
		addProperty("workingDir", workingDir.toPath().toString());
	}

	private void readWorkingDir() {
		String name = "";
		if (conf == null || !conf.containsKey("workingDir")) {
			name = "workingDir";
		}
		else {
			name = conf.getProperty("workingDir");
		}

		File watchDir = new File(name);

		if (!watchDir.exists()) {
			watchDir.mkdirs();
		}
		workingDir = new File(watchDir.getAbsolutePath());
	}

	@Override
	public File getTempCopyDirectory() {
		readTempCopyDir();
		return tempDir;
	}

	private void readTempCopyDir() {
		String name;
		if (conf == null || !conf.containsKey("tempCopyDir")) {
			name = "tempDir";
		}
		else {
			name = conf.getProperty("tempCopyDir");
		}

		File tempCopyDir = new File(name);

		if (!tempCopyDir.exists()) {
			tempCopyDir.mkdirs();
		}

		tempDir = new File(tempCopyDir.getAbsolutePath());
	}

	@Override
	public void setTempCopyDir(File tempCopyDir) {
		addProperty("tempCopyDir", tempCopyDir.toPath().toString());
	}

	private void addProperty(String prop, String value) {
		if (conf == null) {
			conf = new Properties();
		}
		if (conf.contains(prop)) {
			conf.replace(prop, value);
		}
		else {
			conf.put(prop, value);
		}
		configurationReader.saveConfig(conf, configFile);
	}
}
