
package xella.router;

import java.io.*;

/**
 * Policy definitions used by the router
 * Extends this class to provde your own
 *
 */

public class RoutingPolicy {

    private static final int MAX_TTL = 10;

    protected RoutingPolicy() {
    }
    
    /**
     * Messages with higher TTL than this are dropped
     */
    public int getMaxTTL() {
	return MAX_TTL;
    }
}
