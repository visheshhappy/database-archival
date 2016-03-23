/**
 *  Copyright 2016 Jasper Infotech (P) Limited . All Rights Reserved.
 *  JASPER INFOTECH PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.snapdeal.archive.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

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

    @Override
    public String toString() {
        return "DatabaseEntry [id=" + id + ", name=" + name + ", ip=" + ip + ", port=" + port + ", username=" + username + ", password=" + password + "]";
    }

}
