package xella.net;

/**
 * You have to implement this interface to get notified about 
 * network connection changes
 *
 */

public interface ConnectionListener {

    void connecting(ConnectionInfo info);
    void connected(ConnectionInfo info);
    void connectFailed(ConnectionInfo info);
    void hostIgnored(ConnectionInfo info);
    void disconnected(ConnectionInfo info);
    void statusChange(ConnectionInfo info);
}
