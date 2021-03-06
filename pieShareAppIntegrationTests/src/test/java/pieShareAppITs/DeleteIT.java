/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package pieShareAppITs;

import commonTestTools.TestFileUtils;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.inject.Provider;
import org.apache.commons.io.FileUtils;
import org.pieShare.pieShareApp.model.message.fileHistoryMessage.FileDeletedMessage;
import org.pieShare.pieShareApp.model.message.metaMessage.FileTransferCompleteMessage;
import org.pieShare.pieShareApp.task.eventTasks.FileTransferCompleteTask;
import org.pieShare.pieShareApp.task.eventTasks.conflictTasks.FileDeletedTask;
import org.pieShare.pieTools.pieUtilities.service.pieExecutorService.PieExecutorTaskFactory;
import org.pieShare.pieTools.pieUtilities.service.pieExecutorService.api.IPieExecutorTaskFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import pieShareAppITs.helper.ITTasksCounter;
import pieShareAppITs.helper.ITUtil;
import pieShareAppITs.helper.runner.FileSyncMain;
import pieShareAppITs.helper.tasks.TestTask;

/**
 *
 * @author Svetoslav
 */
public class DeleteIT {
	
	private AnnotationConfigApplicationContext context;
	private Process process;
	private List<File> files;
	private String cloudName;
	String password;
	
	public DeleteIT() {
		files = new ArrayList<>();
	}

	@BeforeClass
	public static void setUpClass() throws Exception {
		ITUtil.setUpEnviroment(true);
	}

	@BeforeMethod
	public void setUpMethod() throws Exception {
		
		ITUtil.performTearDownDelete();
		
		for(int i = 0; i< 10; i++) {
			String fileName = String.valueOf(i);
                        File file = new File(ITUtil.getBotWorkingDir(), fileName);
                        file.getParentFile().mkdirs();
                        TestFileUtils.createFile(file, 2);
			files.add(file);
			FileUtils.copyFile(file, new File(ITUtil.getMainWorkingDir(), fileName), true);
		}
		
		this.cloudName = UUID.randomUUID().toString();
		this.password = UUID.randomUUID().toString();
		this.process = ITUtil.startProcess(FileSyncMain.class, cloudName, password);
		
		context = ITUtil.getContext();
		
		IPieExecutorTaskFactory executorFactory = context.getBean("pieExecutorTaskFactory", PieExecutorTaskFactory.class);
		executorFactory.removeTaskRegistration(FileDeletedMessage.class);
		executorFactory.registerTaskProvider(FileDeletedMessage.class, new Provider<TestTask>() {

			@Override
			public TestTask get() {
				return context.getBean(TestTask.class);
			}
		});
		
		IPieExecutorTaskFactory testExecutorFacotry = context.getBean("testTaskFactory", PieExecutorTaskFactory.class);
		testExecutorFacotry.registerTaskProvider(FileDeletedMessage.class, new Provider<FileDeletedTask>() {

			@Override
			public FileDeletedTask get() {
				return context.getBean(FileDeletedTask.class);
			}
		});
	}

	@AfterMethod
	public void tearDownMethod() throws Exception {
		process.destroy();
		files.clear();
		ITUtil.performTearDown(context);
	}
	
	//todo-mr3: this test can reproduce the delete bug we have right now!! see ticket
	@Test(timeOut = 120000)
	public void deleteOneFile() throws Exception {
		ITUtil.waitForProcessToStartup(this.process);
		
		ITTasksCounter counter = context.getBean(ITTasksCounter.class);
		
		ITUtil.executeLoginToTestCloud(context, cloudName, password);
		
		Assert.assertTrue(files.get(4).delete());
		File deletedFile = files.remove(4);
		
		while(counter.getCount(FileDeletedTask.class) <= 0) {
			Thread.sleep(1000);
		}
		
		//todo: test has to be improved (see log)
		//todo: needs to cope with syncMessages at startup
			//todo: implement delete history for no recreating file
		//todo: needs to handle second file deleted message correctly
		if(counter.getCount(FileDeletedTask.class) == 1) {
			File shouldBeDeleted = new File(ITUtil.getMainWorkingDir(), deletedFile.getName());
			Assert.assertFalse(shouldBeDeleted.exists());
			
			//all other files should exist
			for(File file: files) {
				Assert.assertTrue(file.exists());
				Assert.assertTrue((new File(ITUtil.getMainWorkingDir(), file.getName())).exists());
			}
		}
	}
}
