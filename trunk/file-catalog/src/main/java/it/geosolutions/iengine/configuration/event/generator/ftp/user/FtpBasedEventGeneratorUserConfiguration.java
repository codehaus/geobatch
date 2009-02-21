/*
 *  GeoBatch - Open Source geospatial batch processing system
 *  http://geobatch.codehaus.org/
 *  Copyright (C) 2007-2008-2009 GeoSolutions S.A.S.
 *  http://www.geo-solutions.it
 *
 *  GPLv3 + Classpath exception
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package it.geosolutions.iengine.configuration.event.generator.ftp.user;

import java.util.ArrayList;
import java.util.List;
import org.apache.ftpserver.ftplet.Authentication;
import org.apache.ftpserver.ftplet.AuthenticationFailedException;
import org.apache.ftpserver.ftplet.Authority;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.User;
import org.apache.ftpserver.ftplet.UserManager;

import org.apache.ftpserver.usermanager.UsernamePasswordAuthentication;
import org.apache.ftpserver.usermanager.impl.BaseUser;
import org.apache.ftpserver.usermanager.impl.ConcurrentLoginPermission;
import org.apache.ftpserver.usermanager.impl.TransferRatePermission;
import org.apache.ftpserver.usermanager.impl.WritePermission;

/**
 *
 * Helper for single ftp user configuration.
 *
 * @author Ivano Picco
 */

public class FtpBasedEventGeneratorUserConfiguration implements UserManager{

private User userName;

    public User getUserByName(String username) throws FtpException {
        if (doesExist(username)) return userName;
        else return null;
    }

    /*
     * Set a base user with default permission 
     */
    public User setDefaultUser(String login,String password,String ftphome) throws FtpException {
        BaseUser user = new BaseUser();
        user.setName(login);
        user.setPassword(password);
        user.setEnabled(true);
        user.setMaxIdleTime(0);
        user.setHomeDirectory(ftphome);
        List<Authority> auth = new ArrayList<Authority>();
        auth.add(new WritePermission());
        auth.add(new ConcurrentLoginPermission(10, 2));
        auth.add(new TransferRatePermission(0, 0));
        user.setAuthorities(auth);
        userName = user;
        return user;
    }


    public String[] getAllUserNames() throws FtpException {
        String[] users = null;
        users[1] = userName.getName();
        return users;
    }

    public void delete(String username) throws FtpException {
        throw new UnsupportedOperationException("delete() not supported yet.");
    }

    public void save(User user) throws FtpException {
        this.userName = user;
    }

    public boolean doesExist(String username) throws FtpException {
        if (username.equalsIgnoreCase(userName.getName())) return true;
        else return false;
    }

    public User authenticate(Authentication authentication) throws AuthenticationFailedException {

        if (authentication instanceof UsernamePasswordAuthentication) {
            UsernamePasswordAuthentication unpwauth = (UsernamePasswordAuthentication) authentication;
            String un = unpwauth.getUsername();
            String pw = unpwauth.getPassword();
            if (pw.equals(userName.getPassword())) {
                try {
                    return getUserByName(un);
                } catch (FtpException e) {
                    throw new AuthenticationFailedException("Login failed for username: " + un);
                }
            } else {
                throw new AuthenticationFailedException("Login failed for username: " + un);
            }
        } else {
            throw new AuthenticationFailedException("Unknown authentication type: " + authentication.getClass().getName());
        }
    }

    public String getAdminName() throws FtpException {
        throw new UnsupportedOperationException("getAdminName() not supported yet.");
    }

    public boolean isAdmin(String username) throws FtpException {
        throw new UnsupportedOperationException("isAdmin() not supported yet.");
    }

}
