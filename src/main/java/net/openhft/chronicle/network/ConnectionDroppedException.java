package net.openhft.chronicle.network;

import net.openhft.chronicle.core.io.IORuntimeException;

/**
 * thrown when the TcpChannelHub drops its connection to the server
 */
public class ConnectionDroppedException extends IORuntimeException {
    public ConnectionDroppedException(String message) {
        super(message);
    }

    public ConnectionDroppedException(Throwable e) {
        super(e);
    }
}
