/**
 * 
 */

package in.fortytwo42.adapter.controllers;

// TODO: Auto-generated Javadoc

import org.hibernate.Session;

/**
 * The Class IamThreadContext.
 *
 * @author ChiragShah
 */
public final class IamThreadContext {

    /** The Constant ACTOR. */
    private static final ThreadLocal<String> ACTOR = new ThreadLocal<>();

    /** The Constant CORELATION_ID. */
    private static final ThreadLocal<String> CORELATION_ID = new ThreadLocal<>();
    private static final ThreadLocal<Session> SESSION = new ThreadLocal<>();

    /**
     * Instantiates a new iam thread context.
     */
    IamThreadContext() {
        super();
    }

    /**
     * Gets the actor.
     *
     * @return the actor
     */
    public static String getActor() {
        return ACTOR.get();
    }

    /**
     * Sets the actor.
     *
     * @param actor the new actor
     */
    public static void setActor(String actor) {
        ACTOR.set(actor);
    }

    /**
     * Gets the corelation id.
     *
     * @return the corelation id
     */
    public static String getCorelationId() {
        return CORELATION_ID.get();
    }

    /**
     * Sets the corelation id.
     *
     * @param corelationId the new corelation id
     */
    public static void setCorelationId(String corelationId) {
        CORELATION_ID.set(corelationId);
    }
    public static void setSessionWithoutTransaction(Session session) {
        SESSION.set(session);
    }

    public static Session getSessionWithoutTransaction() {
       return SESSION.get();
    }
}
