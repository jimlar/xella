
package xella.router;

import java.io.*;

/**
 * Policy definitions used by the router
 * Extends this class to provde your own
 *
 */

public class RoutingPolicy {

    private static final int MAX_TTL = 10;
    private static final int ROUTER_HISTORY_SIZE = 2000;

    protected RoutingPolicy() {
    }
    
    /**
     * Messages with higher TTL than this are dropped
     */
    public int getMaxTTL() {
	return MAX_TTL;
    }
    
    /**
     * The number of messages the router saves to be able to route back replies
     */
    public int getRouterMessageHistorySize() {
	return ROUTER_HISTORY_SIZE;
    }
}
