/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.guacamole.auth.radius;

import com.google.inject.Inject;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.NoSuchAlgorithmException;
import org.apache.guacamole.GuacamoleException;
import org.apache.guacamole.GuacamoleUnsupportedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.jradius.client.RadiusClient;
import net.jradius.exception.RadiusException;
import net.jradius.packet.RadiusPacket;
import net.jradius.packet.AccessRequest;
import net.jradius.dictionary.*;
import net.jradius.packet.attribute.AttributeList;
import net.jradius.client.auth.RadiusAuthenticator;
import net.jradius.packet.attribute.AttributeFactory;

/**
 * Service for creating and managing connections to RADIUS servers.
 *
 * @author Michael Jumper
 */
public class RadiusConnectionService {

    /**
     * Logger for this class.
     */
    private final Logger logger = LoggerFactory.getLogger(RadiusConnectionService.class);

    /**
     * Service for retrieving RADIUS server configuration information.
     */
    @Inject
    private ConfigurationService confService;


    /**
     * The RADIUS client;
     */
    private RadiusClient radiusClient;

    /**
     * Creates a new instance of RadiusConnection, configured as required to use
     * whichever encryption method is requested within guacamole.properties.
     *
     * @return
     *     A new RadiusConnection instance which has already been configured to
     *     use the encryption method requested within guacamole.properties.
     *
     * @throws GuacamoleException
     *     If an error occurs while parsing guacamole.properties, or if the
     *     requested encryption method is actually not implemented (a bug).
     */
    private void createRadiusConnection() {

        /*
        // Map encryption method to proper connection and socket factory
        EncryptionMethod encryptionMethod = confService.getEncryptionMethod();
        switch (encryptionMethod) {

            // Unencrypted RADIUS connection
            case NONE:
                logger.debug("Connection to RADIUS server without encryption.");
                radiusClient = new RadiusClient();
                return radiusClient;

            // Radius over TTLS (EAP-TTLS)
            case TTLS:
                logger.debug("Connecting to RADIUS server using TTLS.");
                radiusClient = new RadiusClient();
                return radiusClient;

            // We default to unencrypted connections.
            default:
                logger.debug("Defaulting an unencrypted RADIUS connection.");
                radiusClient = new RadiusClient();
                return radiusClient;

        }
        */
        try {
            radiusClient = new RadiusClient(InetAddress.getByName(confService.getRadiusServer()),
                                            confService.getRadiusSharedSecret(),
                                            confService.getRadiusAuthPort(),
                                            confService.getRadiusAcctPort(),
                                            confService.getRadiusTimeout());
        }
        catch (GuacamoleException e) {
            logger.error("Unable to initialize RADIUS client: {}", e.getMessage());
            logger.debug("Failed to init RADIUS client.", e);
            return;
        }
        catch (UnknownHostException e) {
            logger.error("Unable to resolve host: {}", e.getMessage());
            logger.debug("Failed to resolve host.", e);
            return;
        }
        catch (IOException e) {
            logger.error("Unable to communicate with host: {}", e.getMessage());
            logger.debug("Failed to communicate with host.", e);
            return;
        }

    }

    public RadiusPacket authenticate(String username, String password) 
            throws GuacamoleException {

	createRadiusConnection();
        AttributeFactory.loadAttributeDictionary("net.jradius.dictionary.AttributeDictionaryImpl");

        if (radiusClient == null)
            return null;

        if (username == null || username.isEmpty()) {
            logger.warn("Anonymous access not allowed with RADIUS client.");
            return null;
        }
        if (password == null || password.isEmpty()) {
            logger.warn("Password required for RADIUS authentication.");
            return null;
        }

        RadiusAuthenticator radAuth = radiusClient.getAuthProtocol(confService.getRadiusAuthProtocol());
        if (radAuth == null)
            throw new GuacamoleException("Unknown RADIUS authentication protocol.");
        try { 
            AttributeList radAttrs = new AttributeList();
            radAttrs.add(new Attr_UserName(username));
            radAttrs.add(new Attr_UserPassword(password));
            AccessRequest radAcc = new AccessRequest(radiusClient, radAttrs);
            logger.debug("Sending authentication request to radius server for user {}.", username);
            radAuth.setupRequest(radiusClient, radAcc);
            radAuth.processRequest(radAcc);
            return radiusClient.sendReceive(radAcc, confService.getRadiusRetries());
        }
        catch (RadiusException e) {
            logger.error("Unable to complete authentication.", e.getMessage());
            logger.debug("Authentication with RADIUS failed.", e);
            return null;
        }
        catch (NoSuchAlgorithmException e) {
            logger.error("No such RADIUS algorithm: {}", e.getMessage());
            logger.debug("Unknown RADIUS algorithm.", e);
            return null;
        }
    }

    public RadiusPacket authenticate(String username, String state, String response)
            throws GuacamoleException {

        createRadiusConnection();
        AttributeFactory.loadAttributeDictionary("net.jradius.dictionary.AttributeDictionaryImpl");

        if (radiusClient == null)
            return null;

        if (username == null || username.isEmpty()) {
            logger.warn("Anonymous access not allowed with RADIUS client.");
            return null;
        }
        if (state == null || state.isEmpty()) {
            logger.warn("This method needs a previous RADIUS state to respond to.");
            return null;
        }
        if (response == null || response.isEmpty()) {
            logger.warn("Response required for RADIUS authentication.");
            return null;
        }

        RadiusAuthenticator radAuth = radiusClient.getAuthProtocol(confService.getRadiusAuthProtocol());
        if (radAuth == null)
            throw new GuacamoleException("Unknown RADIUS authentication protocol.");
        try {
            AttributeList radAttrs = new AttributeList();
            radAttrs.add(new Attr_UserName(username));
            radAttrs.add(new Attr_State(state));
            radAttrs.add(new Attr_UserPassword(response));
            AccessRequest radAcc = new AccessRequest(radiusClient, radAttrs);
            logger.debug("Sending authentication response to radius server for user {}.", username);
            radAuth.setupRequest(radiusClient, radAcc);
            radAuth.processRequest(radAcc);
            return radiusClient.sendReceive(radAcc, confService.getRadiusRetries());
        }
        catch (RadiusException e) {
            logger.error("Unable to complete authentication.", e.getMessage());
            logger.debug("Authentication with RADIUS failed.", e);
            return null;
        }
        catch (NoSuchAlgorithmException e) {
            logger.error("No such RADIUS algorithm: {}", e.getMessage());
            logger.debug("Unknown RADIUS algorithm.", e);
            return null;
        }
    }

    /**
     * Disconnects the given RADIUS connection, logging any failure to do so
     * appropriately.
     *
     * @param radiusConnection
     *     The RADIUS connection to disconnect.
     */
    public void disconnect() {

        radiusClient.close();

    }

}
