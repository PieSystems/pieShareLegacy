/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pieShare.pieShareApp.service.database;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.pieShare.pieShareApp.model.entities.PieFileEntity;
import org.pieShare.pieShareApp.model.entities.PieFolderEntity;
import org.pieShare.pieShareApp.model.pieFilder.PieFilder;
import org.pieShare.pieShareApp.model.pieFilder.PieFile;
import org.pieShare.pieShareApp.model.pieFilder.PieFolder;
import org.pieShare.pieShareApp.service.database.DAOs.PieFileDAO;
import org.pieShare.pieShareApp.service.database.DAOs.PieFolderDAO;
import org.pieShare.pieShareApp.service.database.api.IModelEntityConverterService;
import org.pieShare.pieShareApp.service.fileService.api.IFileService;
import org.pieShare.pieShareApp.service.folderService.IFolderService;
import org.pieShare.pieShareApp.service.userService.IUserService;
import org.pieShare.pieTools.pieUtilities.service.pieLogger.PieLogger;

/**
 *
 * @author RicLeo00
 */
public class PieFilderDBService {

    private IModelEntityConverterService converterService;
    private PieFileDAO pieFileDAO;
    private PieFolderDAO pieFolderDAO;
    private IUserService userService;
    private IFileService fileService;
    private IFolderService folderService;

    public void setConverterService(IModelEntityConverterService converterService) {
        this.converterService = converterService;
    }

    public void setPieFileDAO(PieFileDAO pieFileDAO) {
        this.pieFileDAO = pieFileDAO;
    }

    public void setPieFolderDAO(PieFolderDAO pieFolderDAO) {
        this.pieFolderDAO = pieFolderDAO;
    }

    public void setUserService(IUserService userService) {
        this.userService = userService;
    }

    public void setFileService(IFileService fileService) {
        this.fileService = fileService;
    }

    public void setFolderService(IFolderService folderService) {
        this.folderService = folderService;
    }

    /*public void persistFilder(PieFilder filder) throws SQLException, IOException {
        File file = new File(userService.getUser().getPieShareConfiguration().getWorkingDir(), filder.getRelativePath());
        String parent = recusriveFilePersister(file.getParentFile(), true);

        if (filder instanceof PieFile) {
            mergePieFile((PieFile) filder, parent, true);
        } else {
            checkOrSaveFolder((PieFolder) filder, parent, true);
        }
    }*/
    public void persistPieFile(PieFile pieFile) throws SQLException, IOException {
        File file = new File(userService.getUser().getPieShareConfiguration().getWorkingDir(), pieFile.getRelativePath());
        String parent = recusrivePieFolderPersister(file.getParentFile(), true);
        mergePieFile(pieFile, parent, true);
    }

    public void persistPieFolder(PieFolder pieFolder) throws SQLException, IOException {
        File file = new File(userService.getUser().getPieShareConfiguration().getWorkingDir(), pieFolder.getRelativePath());
        String parent = recusrivePieFolderPersister(file.getParentFile(), true);
        checkOrSaveFolder(pieFolder, parent, true);
    }

    private String recusrivePieFolderPersister(File file, boolean create) {
        String parentID = null;

        try {
            if (file.getParentFile() == null || file.getCanonicalPath().equals(userService.getUser().getPieShareConfiguration().getWorkingDir().getCanonicalPath())) {
                return null;
            }
            if (!file.getParentFile().getCanonicalPath().equals(userService.getUser().getPieShareConfiguration().getWorkingDir().getCanonicalPath())) {
                parentID = recusrivePieFolderPersister(file.getParentFile(), create);
            }

            return checkOrSaveFolder(folderService.getPieFolder(file), parentID, create);

        } catch (SQLException | IOException ex) {
            PieLogger.error(this.getClass(), "Error pesisting Filder", ex);
        }
        return parentID;
    }

    private String checkOrSaveFolder(PieFolder file, String parent, boolean create) throws SQLException {
        boolean isRoot = false;
        if (parent == null) {
            parent = "root";
            isRoot = true;
        }
        PieFolderEntity rootEntity = pieFolderDAO.findFolderWhereNameANDIsRoot(file.getName(), isRoot, parent);

        if (rootEntity == null && !create) {
            return null;
        }

        if (rootEntity == null && create) {
            rootEntity = converterService.convertToEntity(file);
            rootEntity.setParent(parent);
            rootEntity.setIsRoot(isRoot);
            pieFolderDAO.savePiePieFolder(rootEntity);
        }
        return rootEntity.getId();
    }

    private String mergePieFile(PieFile file, String parent, boolean create) throws SQLException, IOException {
        if (parent == null) {
            parent = "root";
        }

        PieFileEntity fileEntity = pieFileDAO.findAllWhereNameAndParent(file.getName(), parent);

        if (fileEntity == null && !create) {
            return null;
        }

        if (fileEntity == null && create) {
            fileEntity = converterService.convertToEntity(file);
            fileEntity.setParent(parent);
            pieFileDAO.savePieFile(fileEntity);
        }

        pieFileDAO.updatePieFile(fileEntity);

        return fileEntity.getId();
    }

    public List<PieFile> findAllPieFiles() throws SQLException {
        ArrayList<PieFile> files = new ArrayList<>();

        for (PieFileEntity entity : pieFileDAO.findAllPieFiles()) {
            files.add(findFile(entity.getId()));
        }

        return files;
    }

    private PieFile findFile(String id) throws SQLException {
        PieFileEntity file = pieFileDAO.findPieFileById(id);

        if (file == null) {
            return null;
        }

        if (file.getParent().equals("root")) {
            PieFile pieFile = converterService.convertFromEntity(file);
            pieFile.setRelativePath(String.format("%s",file.getFileName()));
            return pieFile;
        }

        PieFolder folder = findFolder(file.getParent());

        PieFile pieFile = converterService.convertFromEntity(file);
        File f = new File(folder.getRelativePath(), file.getFileName());
        pieFile.setRelativePath(f.getPath());
       //pieFile.setRelativePath(String.format("%s%s", folder.getRelativePath(), file.getFileName()));
        return pieFile;
    }

    private PieFolder findFolder(String id) throws SQLException {

        String relativePath;

        PieFolderEntity folder = pieFolderDAO.findPieFolderById(id);

        if (folder == null) {
            return null;
        }

        PieFolder pieFolder = converterService.convertFromEntity(folder);

        if (folder.isIsRoot()) {
            pieFolder.setRelativePath(String.format("%s", folder.getFolderName()));
            return pieFolder;
        }

        PieFolder parent = findFolder(folder.getParent());
        File f = new File(parent.getRelativePath(), pieFolder.getName());
        relativePath = f.getPath(); //String.format("%s%s%s", parent.getRelativePath(), pieFolder.getName(), File.separator);
        pieFolder.setRelativePath(relativePath);
        return pieFolder;
    }

    public List<PieFolder> findAllPieFolders() throws SQLException {
        ArrayList<PieFolder> folders = new ArrayList<>();

        for (PieFolderEntity entity : pieFolderDAO.findAllPieFolders()) {
            folders.add(findFolder(entity.getId()));
        }

        return folders;
    }

    public PieFile findFileByRelativePath(String path) throws SQLException {
        File file = new File(userService.getUser().getPieShareConfiguration().getWorkingDir(), path);
        String parent = recusrivePieFolderPersister(file.getParentFile(), false);

        if (parent == null) {
            parent = "root";
        }

        PieFileEntity fileEntity = pieFileDAO.findAllWhereNameAndParent(file.getName(), parent);

        if (fileEntity == null) {
            return null;
        }

        return findFile(fileEntity.getId());
    }

    public List<PieFile> findPieFileByHash(byte[] hash) throws SQLException {
        List<PieFile> files = new ArrayList<>();

        for (PieFileEntity entity : pieFileDAO.findByMd5(hash)) {
            files.add(findFile(entity.getId()));
        }

        return files;
    }

    public PieFolder findFolderByRelativePath(String path) throws SQLException {
        File file = new File(userService.getUser().getPieShareConfiguration().getWorkingDir(), path);
        String parent = recusrivePieFolderPersister(file.getParentFile(), false);

        if (parent == null) {
            parent = "root";
        }

        PieFolderEntity folderEntity = pieFolderDAO.findFolderWhereNameAndParent(file.getName(), parent);

        if (folderEntity == null) {
            return null;
        }

        return findFolder(folderEntity.getId());
    }

    public void removePieFolder(PieFolder folder) throws SQLException {
        PieFolderEntity entity = this.converterService.convertToEntity(folder);
        pieFolderDAO.deletePieFolder(entity.getId());
    }

    public void mergePieFile(PieFile file) throws SQLException, IOException {
        PieFile pieFile = findFileByRelativePath(file.getRelativePath());

        if (pieFile != null) {
            file.setId(pieFile.getId());
            pieFileDAO.updatePieFile(converterService.convertToEntity(file));
        } else {
            persistPieFile(file);
        }
    }

    public List<PieFile> findAllUnsyncedPieFiles() throws SQLException {
        List<PieFileEntity> qq;
        qq = pieFileDAO.findAllUnsyncedPieFiles();

        ArrayList<PieFile> files = new ArrayList<>();

        for (PieFileEntity entity : qq) {
            files.add(findFile(entity.getId()));
        }
        return files;
    }

    public void resetAllPieFileSynchedFlags() throws SQLException {
        pieFileDAO.resetAllPieFileSynchedFlags();
    }

    public void mergePieFolder(PieFolder folder) throws SQLException, IOException {
        PieFolder pieFolder = findFolderByRelativePath(folder.getRelativePath());

        if (pieFolder != null) {
            folder.setId(pieFolder.getId());
            pieFolderDAO.updatePieFolder(converterService.convertToEntity(folder));
        } else {
            persistPieFolder(folder);
        }
    }

    public List<PieFolder> findAllUnsyncedPieFolders() throws SQLException {
        ArrayList<PieFolder> folders = new ArrayList<>();
        for (PieFolderEntity entity : pieFolderDAO.findAllUnsyncedPieFolders()) {
            folders.add(findFolder(entity.getId()));
        }
        return folders;
    }

    public void resetAllPieFolderSyncedFlags() throws SQLException {
        pieFolderDAO.resetAllPieFolderSynchedFlags();
    }
}
