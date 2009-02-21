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



package it.geosolutions.iengine.flow.event.action.geoserver;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.util.logging.Logger;

/**
 * @author Alessio Fabiani
 * 
 */
public class GeoServerRESTHelper {
    /**
     *
     */
    private static final Logger LOGGER = Logger.getLogger(GeoServerRESTHelper.class.toString());

    /**
     * 
     * @param geoserverREST_URL
     * @param inputStream
     * @param geoserverUser
     * @param geoserverPassword
     * @return
     */
    public static boolean putBinaryFileTo(URL geoserverREST_URL, InputStream inputStream,
            String geoserverUser, String geoserverPassword) {
        boolean res = false;

        try {
            HttpURLConnection con = (HttpURLConnection) geoserverREST_URL.openConnection();
            con.setDoOutput(true);
            con.setDoInput(true);
            con.setRequestMethod("PUT");

            final String login = geoserverUser;
            final String password = geoserverPassword;

            if ((login != null) && (login.trim().length() > 0)) {
                Authenticator.setDefault(new Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(login, password.toCharArray());
                    }
                });
            }

            OutputStream outputStream = con.getOutputStream();

            copyInputStream(inputStream, outputStream);

            if (con.getResponseCode() == HttpURLConnection.HTTP_OK) {
                InputStreamReader is = new InputStreamReader(con.getInputStream());
                String response = readIs(is);
                is.close();
                LOGGER.info("HTTP OK: " + response);
                res = true;
            } else {
                LOGGER.info("HTTP ERROR: " + con.getResponseMessage());
                res = false;
            }
        } catch (MalformedURLException e) {
            LOGGER.info("HTTP ERROR: " + e.getLocalizedMessage());
            res = false;
        } catch (IOException e) {
            LOGGER.info("HTTP ERROR: " + e.getLocalizedMessage());
            res = false;
        }
        return res;

    }

    /**
     * 
     * @param geoserverREST_URL
     * @param inputStream
     * @param geoserverPassword
     * @param geoserverUser
     * @return
     */
    public static boolean putTextFileTo(URL geoserverREST_URL, InputStream inputStream,
            String geoserverPassword, String geoserverUser) {
        boolean res = false;

        try {
            HttpURLConnection con = (HttpURLConnection) geoserverREST_URL.openConnection();
            con.setDoOutput(true);
            con.setDoInput(true);
            con.setRequestMethod("PUT");

            final String login = geoserverUser;
            final String password = geoserverPassword;

            if ((login != null) && (login.trim().length() > 0)) {
                Authenticator.setDefault(new Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(login, password.toCharArray());
                    }
                });
            }

            InputStreamReader inReq = new InputStreamReader(inputStream);
            OutputStreamWriter outReq = new OutputStreamWriter(con.getOutputStream());
            char[] buffer = new char[1024];
            int len;

            while ((len = inReq.read(buffer)) >= 0)
                outReq.write(buffer, 0, len);

            outReq.flush();
            outReq.close();
            inReq.close();

            if (con.getResponseCode() == HttpURLConnection.HTTP_OK) {
                InputStreamReader is = new InputStreamReader(con.getInputStream());
                String response = readIs(is);
                is.close();
                LOGGER.info("HTTP OK: " + response);
                res = true;
            } else {
                LOGGER.info("HTTP ERROR: " + con.getResponseMessage());
                res = false;
            }
        } catch (MalformedURLException e) {
            LOGGER.info("HTTP ERROR: " + e.getLocalizedMessage());
            res = false;
        } catch (IOException e) {
            LOGGER.info("HTTP ERROR: " + e.getLocalizedMessage());
            res = false;
        } finally {
            return res;
        }
    }

    /**
     * 
     * @param geoserverREST_URL
     * @param content
     * @param geoserverUser
     * @param geoserverPassword
     * @return
     */
    public static boolean putContent(URL geoserverREST_URL, String content, String geoserverUser,
            String geoserverPassword) {
        boolean res = false;

        try {
            HttpURLConnection con = (HttpURLConnection) geoserverREST_URL.openConnection();
            con.setDoOutput(true);
            con.setDoInput(true);
            con.setRequestMethod("PUT");

            final String login = geoserverUser;
            final String password = geoserverPassword;

            if ((login != null) && (login.trim().length() > 0)) {
                Authenticator.setDefault(new Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(login, password.toCharArray());
                    }
                });
            }

            OutputStreamWriter outReq = new OutputStreamWriter(con.getOutputStream());
            outReq.write(content);
            outReq.flush();
            outReq.close();

            if (con.getResponseCode() == HttpURLConnection.HTTP_OK) {
                InputStreamReader is = new InputStreamReader(con.getInputStream());
                String response = readIs(is);
                is.close();
                LOGGER.info("HTTP OK: " + response);
                res = true;
            } else {
                LOGGER.info("HTTP ERROR: " + con.getResponseMessage());
                res = false;
            }
        } catch (MalformedURLException e) {
            LOGGER.info("HTTP ERROR: " + e.getLocalizedMessage());
            res = false;
        } catch (IOException e) {
            LOGGER.info("HTTP ERROR: " + e.getLocalizedMessage());
            res = false;
        }
        return res;

    }

    // ////////////////////////////////////////////////////////////////////////
    //
    // HELPER METHODS
    //
    // ////////////////////////////////////////////////////////////////////////

    /**
     * 
     * @param in
     * @param out
     */
    private static void copyInputStream(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int len;

        while ((len = in.read(buffer)) >= 0)
            out.write(buffer, 0, len);

        in.close();
        out.flush();
        out.close();
    }

    /**
     * 
     * @param is
     * @return
     */
    private static String readIs(InputStreamReader is) {
        char[] inCh = new char[1024];
        StringBuffer input = new StringBuffer();
        int r;

        try {
            while ((r = is.read(inCh)) > 0) {
                input.append(inCh, 0, r);
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return input.toString();
    }
}
