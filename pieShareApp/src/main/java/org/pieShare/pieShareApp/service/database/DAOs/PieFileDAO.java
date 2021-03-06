/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pieShare.pieShareApp.service.database.DAOs;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import javax.sql.rowset.serial.SerialBlob;
import org.pieShare.pieShareApp.model.entities.PieFileEntity;
import org.pieShare.pieShareApp.service.database.api.IPieDatabaseManagerFactory;

/**
 *
 * @author RicLeo00
 */
public class PieFileDAO {

    //private final String InsertPieFile = "INSERT INTO PieFile (RelativeFilePath, FileName, LastModified, Deleted, Synched) VALUES (?,?,?,?,?);";
    private final String InsertPieFile = "INSERT INTO PieFile (ID, FileName, LastModified, Deleted, Synched, MD5, Parent) VALUES (?,?,?,?,?,?,?);";
    private final String FindByHash = "SELECT * FROM PieFile WHERE MD5=?";
    private final String SetAllSyncedFalse = "UPDATE PieFile SET Synched=0 WHERE Synched=1;";
    private final String FindAll = "SELECT * FROM PieFile;";
    private final String FindAllUnsyched = "SELECT * FROM PieFile WHERE Synched=0 AND Deleted=0;";
    private final String FindByID = "SELECT * FROM PieFile WHERE ID=?;";
    private final String UpdatePieFile = "UPDATE PieFile SET FileName=?, LastModified=?, Deleted=?, Synched=?, MD5=? WHERE ID=?;";

    private final String FindAllWhereNameAndParent = "SELECT * FROM PieFile WHERE FileName=? AND Parent=?;";
    
    //private final String DeletePieFile = "DELETE FROM PieFile WHERE AbsoluteWorkingPath=?";
    private IPieDatabaseManagerFactory databaseFactory;

    public void setDatabaseFactory(IPieDatabaseManagerFactory databaseFactory) {
        this.databaseFactory = databaseFactory;
    }

    public void savePieFile(PieFileEntity pieFileEntity) throws SQLException {

        Connection con = databaseFactory.getDatabaseConnection();

        try (PreparedStatement insertInto = con.prepareStatement(InsertPieFile)) {
            insertInto.setString(1, pieFileEntity.getId());
            insertInto.setString(2, pieFileEntity.getFileName());

            insertInto.setLong(3, pieFileEntity.getLastModified());

            int val = 0;
            if (pieFileEntity.isDeleted()) {
                val = 1;
            }
            insertInto.setInt(4, val);

            val = 0;
            if (pieFileEntity.isSynched()) {
                val = 1;
            }
            insertInto.setInt(5, val);
            insertInto.setBytes(6, pieFileEntity.getMd5());
            insertInto.setString(7, pieFileEntity.getParent());

            insertInto.executeUpdate();
        }
    }

    public void updatePieFile(PieFileEntity pieFileEntity) throws SQLException {

        Connection con = databaseFactory.getDatabaseConnection();

        try (PreparedStatement updateQuery = con.prepareStatement(UpdatePieFile)) {
            //updateQuery.setString(1, pieFileEntity.getRelativeFilePath());
            updateQuery.setString(1, pieFileEntity.getFileName());

            updateQuery.setLong(2, pieFileEntity.getLastModified());

            int val = 0;
            if (pieFileEntity.isDeleted()) {
                val = 1;
            }
            updateQuery.setInt(3, val);

            val = 0;
            if (pieFileEntity.isSynched()) {
                val = 1;
            }
            updateQuery.setInt(4, val);

            updateQuery.setBytes(5, pieFileEntity.getMd5());
            updateQuery.setString(6, pieFileEntity.getId());
            updateQuery.executeUpdate();
        }
    }

    public void resetAllPieFileSynchedFlags() throws SQLException {
        Connection con = databaseFactory.getDatabaseConnection();

        try (Statement stmt = con.createStatement()) {
            stmt.executeUpdate(SetAllSyncedFalse);
        }
    }

    public PieFileEntity findPieFileById(String id) throws SQLException {

        Connection con = databaseFactory.getDatabaseConnection();
        List<PieFileEntity> entities;

        try (PreparedStatement findById = con.prepareStatement(FindByID)) {
            findById.setString(1, id);
            ResultSet results = findById.executeQuery();
            entities = createFromResult(results);
        }
        if (entities.isEmpty()) {
            return null;
        }

        return entities.get(0);
    }
    
     public List<PieFileEntity> findByMd5(byte[] md5) throws SQLException {

        Connection con = databaseFactory.getDatabaseConnection();
        List<PieFileEntity> entities;

        try (PreparedStatement findById = con.prepareStatement(FindByHash)) {
            findById.setBytes(1, md5);
            ResultSet results = findById.executeQuery();
            entities = createFromResult(results);
        }
        return entities;
    }

    
    public PieFileEntity findAllWhereNameAndParent(String name, String parent) throws SQLException {

        Connection con = databaseFactory.getDatabaseConnection();
        List<PieFileEntity> entities;

        try (PreparedStatement findById = con.prepareStatement(FindAllWhereNameAndParent)) {
            findById.setString(1, name);
            findById.setString(2, parent);
            ResultSet results = findById.executeQuery();
            entities = createFromResult(results);
        }
        if (entities.isEmpty()) {
            return null;
        }

        return entities.get(0);
    }

    
    public List<PieFileEntity> findAllUnsyncedPieFiles() throws SQLException {
        return findAllSQL(FindAllUnsyched);
    }

    public List<PieFileEntity> findAllPieFiles() throws SQLException {
        return findAllSQL(FindAll);
    }

    private List<PieFileEntity> findAllSQL(String sql) throws SQLException {
        Connection con = databaseFactory.getDatabaseConnection();
        List<PieFileEntity> entities;

        try (PreparedStatement findAllQuery = con.prepareStatement(sql)) {
            ResultSet results = findAllQuery.executeQuery();
            entities = createFromResult(results);
        }
        return entities;
    }

    private List<PieFileEntity> createFromResult(ResultSet results) throws SQLException {
        List<PieFileEntity> entities;
        entities = new ArrayList<>();

        while (results.next()) {
            PieFileEntity entity = new PieFileEntity();
            entity.setId(results.getString("ID"));
            entity.setFileName(results.getString("FileName"));
            entity.setLastModified(results.getLong("LastModified"));
            entity.setDeleted(results.getInt("Deleted") != 0);
            entity.setSynched(results.getInt("Synched") != 0);
            entity.setMd5(results.getBytes("MD5"));
            entity.setParent(results.getString("Parent"));
            entities.add(entity);
        }

        return entities;
    }
}
