
package xella.demo;

import java.util.*;

import javax.swing.table.TableModel;
import javax.swing.event.TableModelListener;
import javax.swing.event.TableModelEvent;

import xella.net.*;


public class ConnectionsTableModel 
    implements TableModel, ConnectionListener
{
    private static final String[] COLUMN_NAMES = new String[] {"Host", 
							       "Sent", 
							       "Received",
							       "Dropped",
							       "Status"};
    

    private Collection listeners = new ArrayList();
    private List connections = new ArrayList();

    public ConnectionsTableModel(GnutellaEngine engine) {
	engine.addConnectionListener(this);
    }
	
    /* -- this ConnectionListener interface impl --*/

    public void connecting(ConnectionInfo info) {
	connections.add(info);
	signalListeners();
    }

    public void connected(ConnectionInfo info) {
	signalListeners();
    }

    public void statusChange(ConnectionInfo info) {
	signalListeners();	    
    }

    public void connectFailed(ConnectionInfo info) {
	connections.remove(info);
	signalListeners();
    }

    public void disconnected(ConnectionInfo info) {
	connections.remove(info);
	signalListeners();
    }

    public void hostIgnored(Host host) {}
    public void hostDiscovered(Host host) {}


    /* -- the TableModel interface impl -- */

    public void addTableModelListener(TableModelListener l) {
	listeners.add(l);
    }
    public void removeTableModelListener(TableModelListener l) {
	listeners.remove(l);
    }
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
    public int getRowCount() {
	return connections.size();
    }
    public Object getValueAt(int rowIndex, int columnIndex) {
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
    public boolean isCellEditable(int rowIndex, int columnIndex) {
	return false;
    } 
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
	return;
    }

    /* -- helpers -- */

    private void signalListeners() {
	Iterator iter = listeners.iterator();
	while (iter.hasNext()) {
	    TableModelListener listener = (TableModelListener) iter.next();
	    listener.tableChanged(new TableModelEvent(this));
	}
    }
}
