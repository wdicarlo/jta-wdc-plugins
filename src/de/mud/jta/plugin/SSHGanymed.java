/*
 * JTA plugin to use Apache Telnet in JTA
 * 
 * (c) Walter Di Carlo 2010. All Rights Reserved.
 *
 * --LICENSE NOTICE--
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 * --LICENSE NOTICE--
 *
 */

package de.mud.jta.plugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.swing.JOptionPane;


import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.KnownHosts;
import ch.ethz.ssh2.Session;
import ch.ethz.ssh2.StreamGobbler;

import de.mud.jta.FilterPlugin;
import de.mud.jta.Plugin;
import de.mud.jta.PluginBus;
import de.mud.jta.PluginConfig;
import de.mud.jta.event.ConfigurationListener;
import de.mud.jta.event.OnlineStatus;
import de.mud.jta.event.SocketListener;
import de.mud.jta.plugin.ssh.AdvancedVerifier;

public class SSHGanymed extends Plugin implements FilterPlugin, SocketListener {

    private final static int debug = 0;
    private String           error = null;
//    private TelnetClient     _telnet = new TelnetClient();
    private InputStream      _in;
    private OutputStream     _out;
    private boolean          online;
    
//begin add @sp 04.05.11 SSH Plugin  
    public static final String knownHostPath = "/de/mud/jta/plugin/ssh/known_hosts";
    Connection conn = null;
    Session session = null;
    KnownHosts database = new KnownHosts();
//end add @sp 04.05.11 SSH Plugin
    /**
     * Create a new socket plugin.
     */
    public SSHGanymed(final PluginBus bus, final String id) {
        super(bus, id);
//begin add @sp 04.05.11 SSH Plugin
        loadKnownHosts();
//end add @sp 04.05.11 SSH Plugin    
        // register socket listener
        bus.registerPluginListener(this);

        bus.registerPluginListener(new ConfigurationListener() {
            public void setConfiguration(PluginConfig config) {
                String conf = config.getProperty("SSHGanymed", id, "conf");
            }
        });

        //        // register an online status listener
        //        bus.registerPluginListener(new OnlineStatusListener() {
        //
        //
        //            public void online() {
        //                online = true;
        //            }
        //
        //            public void offline() {
        //                online = false;
        //            }
        //        });

    }

    private void loadKnownHosts(){

        File knownHostFile = new File(knownHostPath);
    	if (knownHostFile.exists()){
    		try {
    			database.addHostkeys(knownHostFile);
    		} catch (IOException e) {
    		}
    	}
    }
    /**
     * Connect to the host and port passed. If the multi relayd (mrelayd) is
     * used to allow connections to any host and the Socket.relay property
     * is configured this method will connect to the relay first, send
     * off the string "relay host port\n" and then the real connection will
     * be published to be online.
     */
    public void connect(String host, int port) throws IOException {
        if (host == null) {
            return;
        }
        if (debug > 0){
            error("connect(" + host + "," + port + ")");
        }
        try {
        	
        	String username = System.getProperty("telnet.username");
        	String password = System.getProperty("telnet.password");
            // Connect to the specified server
//            _telnet.connect(host, port);
    		conn = new Connection(host);
    			/*
    			 * CONNECT AND VERIFY SERVER HOST KEY (with callback)
    			 */

    			String[] hostkeyAlgos = database.getPreferredServerHostkeyAlgorithmOrder(host);
    
    			if (hostkeyAlgos != null)
    				conn.setServerHostKeyAlgorithms(hostkeyAlgos);

    			conn.connect(new AdvancedVerifier(database));

    			/*
    			 * AUTHENTICATION PHASE
    			 */

    			while (true) {
    	        	
    				if (conn.isAuthMethodAvailable(username, "password"))
    				{
    					boolean res = conn.authenticateWithPassword(username, password);

    					if (res == true)
    						break;

//    					lastError = "Password authentication failed."; // try again, if possible

    					continue;
    				}
//    				throw new IOException("No supported authentication methods available.");
    			}

//begin @sp 21.07.11 TEST     			
        } catch (Exception e) {
        	
        }
        try {
//end @sp 21.07.11 TEST 
    			/*
    			 * 
    			 * AUTHENTICATION OK. DO SOMETHING.
    			 * 
    			 */

    			session = conn.openSession();

    			int x_width = 85;
    			int y_width = 24;

    			session.requestPTY("dumb", x_width, y_width, 0, 0, null);
    			session.startShell();

//    			TerminalDialog td = new TerminalDialog(loginFrame, username + "@" + hostname, sess, x_width, y_width);

    			/* The following call blocks until the dialog has been closed */

//    			td.setVisible(true);
    			
//            // Get input and output stream references
//            _in = _telnet.getInputStream();
//            _out = _telnet.getOutputStream();
        	_in = new StreamGobbler(session.getStdout());
            _out = session.getStdin();
			online = true;
	        bus.broadcast(new OnlineStatus(online));
          
        } catch (Exception e) {
            error = "Sorry, Could not connect to: " + host + " " + port + "\r\n" + "Reason: " + e + "\r\n\r\n";
//begin add @sp 12.05.11 handle connection error
//begin del @sp 20.07.11 TEST: IGNORE ERROR MSG FROM SERVER           
//            JOptionPane.showMessageDialog(
//                    null, 
//                    "An error occurred while connecting to " + host + "\r\n" + "Reason: " + e.getMessage() + "\r\n\r\n", 
//                    "Connection Error", 
//                    JOptionPane.INFORMATION_MESSAGE);
//            disconnect();
//            System.exit(0);
//end del @sp 20.07.11 TEST: IGNORE ERROR MSG FROM SERVER     
//end add @sp 12.05.11 handle connection error 
        } 
    }

//    public boolean isConnected() {
//        return _telnet.isConnected();
//    }

    /** Disconnect the socket and close the connection. */
    public void disconnect() throws IOException {
        if (online == false
//        		|| isConnected() == false
        		) {
            return;
        }
        if (debug > 0)
            error("disconnect()");
        online = false;
        bus.broadcast(new OnlineStatus(false));
        try {
            _in.close();
            _out.close();
//            _telnet.disconnect();
            if(session!=null){
            	session.close();
            }
        	if(conn!=null){
        		conn.close();
        	}
        } catch (Exception e) {
            error("disconnect() - cannot close everything");
        }
    }

    public void setFilterSource(FilterPlugin plugin) {
        // we do not have a source other than our socket
    }

    public FilterPlugin getFilterSource() {
        return null;
    }

//    public int read(byte[] b) throws IOException {
//
////begin del @sp 04.05.11 SSH Plugin
//        if (online == false
////        		|| isConnected() == false
//        		) {
//            disconnect();
//            return -1;
//        }
////end del @sp 04.05.11 SSH Plugin
//
//        // send error messages upward
//        if (error != null && error.length() > 0) {
//        	
//        	
//            int n = error.length() < b.length ? error.length() : b.length;
//            System.arraycopy(error.getBytes(), 0, b, 0, n);
//            error = error.substring(n);
//            return n;
//        }
//        if (_in == null) {
//            disconnect();
//            return -1;
//        }
//        int n = _in.read(b);
//        if (n < 0)
//            disconnect();
//
//        return n;
//    }

    public int read(byte[] b) throws IOException {
    	return _in.read(b);
    }
    
    public void write(byte[] b) throws IOException {
        if (_out == null)
            return;
        if (online == false
//        		|| isConnected() == false
        		) {
            disconnect();
            return;
        }
//begin del @sp 04.05.11 SSH Plugin
//        if (((char) b[0]) == '\r' && b.length == 1) {
//            b = new byte[2];
//            b[0] = '\r';
//            b[1] = '\n';
//        }
//end del @sp 04.05.11 SSH Plugin
        try {
            _out.write(b);
            _out.flush();
        } catch (IOException e) {
            disconnect();
        }
    }   
}
