
package xella.demo;

import java.util.*;

import javax.swing.table.AbstractTableModel;
import javax.swing.event.TableModelListener;
import javax.swing.event.TableModelEvent;

import xella.net.*;


public class ConnectionsTableModel extends AbstractTableModel
    implements ConnectionListener
{
    private static final String[] COLUMN_NAMES = new String[] {"Host", 
							       "Sent", 
							       "Received",
							       "Dropped",
							       "Status"};
    
    private List connections = new ArrayList();

    public ConnectionsTableModel(GnutellaEngine engine) {
	engine.addConnectionListener(this);
    }
	
    /* -- this ConnectionListener interface impl --*/

    public synchronized void connecting(ConnectionInfo info) {
	connections.add(info);
	//fireTableDataChanged();
	fireTableRowsInserted(connections.size() - 1, 
			      connections.size() - 1);
    }
    
    public synchronized void connected(ConnectionInfo info) {
	int row = connections.indexOf(info);
	fireTableRowsUpdated(row, row);
	//fireTableDataChanged();
    }

    public synchronized void statusChange(ConnectionInfo info) {
	int row = connections.indexOf(info);
	fireTableRowsUpdated(row, row);
	//fireTableDataChanged();
    }

    public synchronized void connectFailed(ConnectionInfo info) {
	int row = connections.indexOf(info);
	connections.remove(info);
	//fireTableDataChanged();
	fireTableRowsDeleted(row, row);
    }

    public synchronized void disconnected(ConnectionInfo info) {
	int row = connections.indexOf(info);
	connections.remove(info);
	//fireTableDataChanged();
	fireTableRowsDeleted(row, row);
    }

    public void hostIgnored(Host host) {}
    public void hostDiscovered(Host host) {}


    /* -- the TableModel interface impl -- */

    public Class getColumnClass(int columnIndex) {
	switch (columnIndex) {
	case 1:
	case 2:
	case 3:
	    return Integer.class;
	default:
	    return String.class;
	}
    } 
    public int getColumnCount() {
	return COLUMN_NAMES.length;
    }
    public String getColumnName(int columnIndex) {
	return COLUMN_NAMES[columnIndex];
    }
    public synchronized int getRowCount() {
	return connections.size();
    }
    public synchronized Object getValueAt(int rowIndex, int columnIndex) {
	if (rowIndex >= connections.size()) {
	    return null;
	}
	ConnectionInfo info = (ConnectionInfo) connections.get(rowIndex);
	switch(columnIndex) {
	case 0:
	    return info.getHost();
	case 1:
	    return new Integer(info.getMessagesSent());
	case 2:
	    return new Integer(info.getMessagesReceived());
	case 3:
	    return new Integer(info.getMessagesDropped());
	case 4:
	    return info.getStatusMessage();
	default:
	    return null;
	}
    }
}
