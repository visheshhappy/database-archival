/**
 *  Copyright 2016 Jasper Infotech (P) Limited . All Rights Reserved.
 *  JASPER INFOTECH PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.snapdeal.archive.entity;

import java.io.Serializable;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.snapdeal.archive.DatabaseType;

/**
 * @version 1.0, 16-Mar-2016
 * @author vishesh
 */
@Entity
@Table(name = "database_entry")
public class DatabaseEntry implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 7752910954988589523L;

    @Column(name = "id")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long              id;

    @Column(name = "name")
    private String            name;

    @Column(name = "ip")
    private String            ip;

    @Column(name = "port")
    private String            port;

    @Column(name = "username")
    private String            username;

    @Column(name = "password")
    private String            password;

    public enum DatabaseServer {
        MYSQL, ORACLE
    }

    @Column(name = "database_server")
    @Enumerated(value = EnumType.STRING)
    private DatabaseServer     databaseServer;

    @Column(name = "driver_class")
    private String             driverClass;

    @Column(name = "is_active")
    private Boolean            active;

    @Column(name = "database_name")
    private String             databaseName;

    @Column(name = "extra_parameter")
    private String             extraParameter;

    @ManyToOne(cascade = { CascadeType.ALL })
    @JoinColumn(name = "archive_information_id")
    private ArchiveInformation archiveInformation;

    @Column(name = "database_type")
    @Enumerated(value = EnumType.STRING)
    private DatabaseType       databaseType;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public DatabaseServer getDatabaseServer() {
        return databaseServer;
    }

    public void setDatabaseServer(DatabaseServer databaseServer) {
        this.databaseServer = databaseServer;
    }

    public String getDriverClass() {
        return driverClass;
    }

    public void setDriverClass(String driverClass) {
        this.driverClass = driverClass;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    public String getExtraParameter() {
        return extraParameter;
    }

    public void setExtraParameter(String extraParameter) {
        this.extraParameter = extraParameter;
    }

    public ArchiveInformation getArchiveInformation() {
        return archiveInformation;
    }

    public void setArchiveInformation(ArchiveInformation archiveInformation) {
        this.archiveInformation = archiveInformation;
    }

    public DatabaseType getDatabaseType() {
        return databaseType;
    }

    public void setDatabaseType(DatabaseType databaseType) {
        this.databaseType = databaseType;
    }

    @Override
    public String toString() {
        return "DatabaseEntry [id=" + id + ", name=" + name + ", ip=" + ip + ", port=" + port + ", username=" + username + ", password=" + password + ", databaseType="
                + databaseServer + ", driverClass=" + driverClass + ", active=" + active + ", databaseName=" + databaseName + ", extraParameter=" + extraParameter + "]";
    }

}
