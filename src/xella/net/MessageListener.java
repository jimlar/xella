package xella.net;

/**
 * You have to implement this interface to be able to receive messages
 *
 */

public interface MessageListener {

    void receivedPing(PingMessage message);
    void receivedPong(PongMessage message);
    void receivedPush(PushMessage message);
    void receivedQuery(QueryMessage message);
    void receivedQueryResponse(QueryResponseMessage message);
}
