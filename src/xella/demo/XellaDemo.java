/*
 * XellaDemo.java
 *
 * Created on den 13 juli 2001, 16:03
 */

package xella.demo;

import java.io.IOException;
import java.util.*;
import xella.net.*;

/**
 *
 * @author  jimmy
 */
public class XellaDemo extends javax.swing.JFrame implements MessageListener, ConnectionListener {

    private GnutellaEngine engine;
    private QueryMessage activeQuery;

    private int numPings = 0;
    private int numPongs = 0;
    private int numPushes = 0;
    private int numQueries = 0;
    private int numQueryResponses = 0;

    private int numHostsConnecting = 0;
    private int numHostsConnected = 0;
    private int numConnectFailed = 0;
    private int numHostsDisconnected = 0;
    private int numHostsIgnored = 0;


    /** Creates new form XellaDemo */
    public XellaDemo() throws IOException {
        initComponents();
	updateStatistics();
        this.engine = new GnutellaEngine(1, 1, 6346);
	engine.addMessageListener(this);
	engine.addConnectionListener(this);
	engine.start();

	engine.addHost("127.0.0.1", 2944);
// 	engine.addHost("gnutellahosts.com", 6346);
// 	engine.addHost("router.limewire.com", 6346);
//         engine.addHost("gnutella.hostscache.com", 6346);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        jTabbedPane2 = new javax.swing.JTabbedPane();
        jPanel2 = new javax.swing.JPanel();
        jPanel5 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        searchTextField = new javax.swing.JTextField();
        jButton3 = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        searchResultsTable = new javax.swing.JTable();
        jPanel4 = new javax.swing.JPanel();
        jPanel8 = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        numPingsLabel = new javax.swing.JLabel();
        numPongsLabel = new javax.swing.JLabel();
        numPushesLabel = new javax.swing.JLabel();
        numQueriesLabel = new javax.swing.JLabel();
        numQueryResponsesLabel = new javax.swing.JLabel();
        numMessagesLabel = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        hostsConnectingLabel = new javax.swing.JLabel();
        hostsConnectedLabel = new javax.swing.JLabel();
        hostsConnectFailedLabel = new javax.swing.JLabel();
        hostsDisconnectedLabel = new javax.swing.JLabel();
        hostsIgnoredLabel = new javax.swing.JLabel();
        hostsKnownLabel = new javax.swing.JLabel();
        jPanel6 = new javax.swing.JPanel();
        jPanel7 = new javax.swing.JPanel();
        
        setTitle("Xella demo");
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                exitForm(evt);
            }
        });
        
        jPanel2.setLayout(new java.awt.BorderLayout(0, 10));
        
        jPanel5.setLayout(new java.awt.BorderLayout(2, 2));
        
        jLabel2.setText("Search words:");
        jLabel2.setForeground(java.awt.Color.black);
        jLabel2.setFont(new java.awt.Font("Dialog", 0, 12));
        jPanel5.add(jLabel2, java.awt.BorderLayout.WEST);
        
        jPanel5.add(searchTextField, java.awt.BorderLayout.CENTER);
        
        jButton3.setFont(new java.awt.Font("Dialog", 0, 12));
        jButton3.setText("Go");
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                startSearchHandler(evt);
            }
        });
        
        jPanel5.add(jButton3, java.awt.BorderLayout.EAST);
        
        jPanel2.add(jPanel5, java.awt.BorderLayout.NORTH);
        
        searchResultsTable.setModel(new javax.swing.table.DefaultTableModel(
        new Object [][] {
            
        },
        new String [] {
            "File", "Speed", "Host"
        }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.Integer.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false
            };
            
            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
            
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane2.setViewportView(searchResultsTable);
        
        jPanel2.add(jScrollPane2, java.awt.BorderLayout.CENTER);
        
        jTabbedPane2.addTab("Search", jPanel2);
        
        jPanel4.setLayout(new java.awt.BorderLayout());
        
        jPanel8.setLayout(new java.awt.GridLayout(2, 2, 10, 10));
        
        jPanel1.setLayout(new java.awt.GridLayout(6, 1));
        
        jPanel1.setBorder(new javax.swing.border.TitledBorder("Messages"));
        numPingsLabel.setText("Pings: 0");
        numPingsLabel.setFont(new java.awt.Font("Dialog", 0, 11));
        jPanel1.add(numPingsLabel);
        
        numPongsLabel.setText("Pongs: 0");
        numPongsLabel.setFont(new java.awt.Font("Dialog", 0, 11));
        jPanel1.add(numPongsLabel);
        
        numPushesLabel.setText("Pushes: 0");
        numPushesLabel.setFont(new java.awt.Font("Dialog", 0, 11));
        jPanel1.add(numPushesLabel);
        
        numQueriesLabel.setText("Queries: 0");
        numQueriesLabel.setFont(new java.awt.Font("Dialog", 0, 11));
        jPanel1.add(numQueriesLabel);
        
        numQueryResponsesLabel.setText("Query reponses: 0");
        numQueryResponsesLabel.setFont(new java.awt.Font("Dialog", 0, 11));
        jPanel1.add(numQueryResponsesLabel);
        
        numMessagesLabel.setText("Total: 0");
        numMessagesLabel.setFont(new java.awt.Font("Dialog", 0, 11));
        jPanel1.add(numMessagesLabel);
        
        jPanel8.add(jPanel1);
        
        jPanel3.setLayout(new java.awt.GridLayout(6, 1));
        
        jPanel3.setBorder(new javax.swing.border.TitledBorder("Hosts"));
        hostsConnectingLabel.setText("Connecting: 0");
        hostsConnectingLabel.setFont(new java.awt.Font("Dialog", 0, 11));
        jPanel3.add(hostsConnectingLabel);
        
        hostsConnectedLabel.setText("Connected: 0");
        hostsConnectedLabel.setFont(new java.awt.Font("Dialog", 0, 11));
        jPanel3.add(hostsConnectedLabel);
        
        hostsConnectFailedLabel.setText("Connect failed: 0");
        hostsConnectFailedLabel.setFont(new java.awt.Font("Dialog", 0, 11));
        jPanel3.add(hostsConnectFailedLabel);
        
        hostsDisconnectedLabel.setText("Disconnected: 0");
        hostsDisconnectedLabel.setFont(new java.awt.Font("Dialog", 0, 11));
        jPanel3.add(hostsDisconnectedLabel);
        
        hostsIgnoredLabel.setText("Ignored hosts: 0");
        hostsIgnoredLabel.setFont(new java.awt.Font("Dialog", 0, 11));
        jPanel3.add(hostsIgnoredLabel);
        
        hostsKnownLabel.setText("Known hosts: 0");
        hostsKnownLabel.setFont(new java.awt.Font("Dialog", 0, 11));
        jPanel3.add(hostsKnownLabel);
        
        jPanel8.add(jPanel3);
        
        jPanel6.setBorder(new javax.swing.border.TitledBorder("Downloads"));
        jPanel8.add(jPanel6);
        
        jPanel7.setBorder(new javax.swing.border.TitledBorder("Uploads"));
        jPanel8.add(jPanel7);
        
        jPanel4.add(jPanel8, java.awt.BorderLayout.NORTH);
        
        jTabbedPane2.addTab("Statistics", jPanel4);
        
        getContentPane().add(jTabbedPane2, java.awt.BorderLayout.CENTER);
        
        pack();
    }//GEN-END:initComponents

    private void startSearchHandler(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_startSearchHandler
        // Add your handling code here:
        try {
            QueryMessage message = MessageFactory.getInstance().createQueryMessage(0, searchTextField.getText());
            this.activeQuery = message;
            engine.send(message);
        } catch (IOException e) {
            System.out.println("Cant start search: " + e);
            e.printStackTrace();
        }
    }//GEN-LAST:event_startSearchHandler

    /** Exit the Application */
    private void exitForm(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_exitForm
        System.exit(0);
    }//GEN-LAST:event_exitForm

    /**
    * @param args the command line arguments
    */
    public static void main(String args[]) throws Exception {
        new XellaDemo().show();
    }


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTabbedPane jTabbedPane2;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JTextField searchTextField;
    private javax.swing.JButton jButton3;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTable searchResultsTable;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JLabel numPingsLabel;
    private javax.swing.JLabel numPongsLabel;
    private javax.swing.JLabel numPushesLabel;
    private javax.swing.JLabel numQueriesLabel;
    private javax.swing.JLabel numQueryResponsesLabel;
    private javax.swing.JLabel numMessagesLabel;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JLabel hostsConnectingLabel;
    private javax.swing.JLabel hostsConnectedLabel;
    private javax.swing.JLabel hostsConnectFailedLabel;
    private javax.swing.JLabel hostsDisconnectedLabel;
    private javax.swing.JLabel hostsIgnoredLabel;
    private javax.swing.JLabel hostsKnownLabel;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    // End of variables declaration//GEN-END:variables

    
    public void receivedPing(PingMessage message) {
	numPings++;
	updateStatistics();
	try {
	    /* Pong the pings */
	    PongMessage pongMessage 
		= MessageFactory.getInstance().createPongMessage(message,
								 "192.168.1.31", 
								 6346, 
								 12, 
								 1234567890);
	    engine.send(pongMessage);	
	} catch (IOException e) {
	    e.printStackTrace();
	}
    }

    public void receivedPong(PongMessage message) {numPongs++; updateStatistics();}
    public void receivedPush(PushMessage message) {numPushes++; updateStatistics();}
    public void receivedQuery(QueryMessage message) {numQueries++; updateStatistics();}
    public void receivedQueryResponse(QueryResponseMessage message) {
	numQueryResponses++; 
	updateStatistics();
        if (message.isResponseFor(activeQuery)) {
           System.out.println("OUR RESPONSE: " + message);

	   String host = message.getHostIP();
	   Integer speed = new Integer(message.getHostSpeed());
	   Iterator iter = message.getQueryHits().iterator();
	   while (iter.hasNext()) {
	       QueryHit hit = (QueryHit) iter.next();
	       ((javax.swing.table.DefaultTableModel) searchResultsTable.getModel()).addRow(new Object[] {hit.getFileName(), speed, host});
	   }
        } 
    }

    private void updateStatistics() {
	numPingsLabel.setText("Pings: " + numPings);
	numPongsLabel.setText("Pongs: " + numPongs);
	numPushesLabel.setText("Pushes: " + numPushes);
	numQueriesLabel.setText("Queries: " + numQueries);
	numQueryResponsesLabel.setText("Query reponses: " + numQueryResponses);
	numMessagesLabel.setText("Total: " + (numPings + numPongs + numPushes + numQueries + numQueryResponses)); 

	hostsConnectedLabel.setText("Connected: " + numHostsConnected);
	hostsDisconnectedLabel.setText("Disconnected: " + numHostsDisconnected);
	hostsConnectFailedLabel.setText("Connect failed: " + numConnectFailed);
	hostsConnectingLabel.setText("Connecting: " + numHostsConnecting);
	hostsIgnoredLabel.setText("Ignored hosts: " + numHostsIgnored);
    }

    public synchronized void connecting(ConnectionInfo info) {
	numHostsConnecting++;
	updateStatistics();
    }
    
    public synchronized void connected(ConnectionInfo info) {
	numHostsConnected++;
	numHostsConnecting--;
	updateStatistics();
    }
    
    public synchronized void connectFailed(ConnectionInfo info) {
	numConnectFailed++;
	numHostsConnecting--;
	updateStatistics();
    }
    
    public synchronized void hostIgnored(ConnectionInfo info) {
	numHostsIgnored++;
	updateStatistics();
    }
    
    public synchronized void disconnected(ConnectionInfo info) {
	numHostsDisconnected++;
	numHostsConnected--;
	updateStatistics();
	System.out.println("Disconnected " + info.getHost() 
			   + " (sent " + info.getNumMessagesSent() 
			   + ", received " + info.getNumMessagesReceived() 
			   + "), " + info.getStatusMessage());
    }

    public synchronized void statusChange(ConnectionInfo info) {}
}
