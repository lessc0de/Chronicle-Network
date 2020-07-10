package net.openhft.chronicle.network.internal.roundrobin;

import net.openhft.chronicle.bytes.Bytes;
import net.openhft.chronicle.network.NetworkContext;
import net.openhft.chronicle.network.api.TcpHandler;
import net.openhft.chronicle.network.tcp.ISocketChannel;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.stream.IntStream;

/**
 * This class provides copy-free buffered reading from and writing to a socket and
 * seamlessly connects a TcpHandler to the internal buffers.
 * <p>
 * The class will allocate two arrays of fixed-sized Bytes where each
 * Bytes capacity is twice the maximum messages size. The default
 * max message size is 16MB so providing 3 as size will allocate
 * 3 * 2 * 16MB = 96 MB data buffers.
 * <p>
 * This class is not thread-safe.
 * <p>
 * There are four active buffers at any given time used to:
 * <ul>
 * <li>fromSocketBuffer     read from the socket</li>
 * <li>consumerBuffer       consume buffered messages from the socket</li>
 * <li>producerBuffer       produce buffered messages to the socket</li>
 * <li>toSocketBuffer       write to the socket</li>
 * </ul>
 * Initially, the {@code fromSocketBuffer} and {@code consumerBuffer} are the same
 * but if the consumer gets stalled, the {@code fromSocketBuffer} might advance.
 * <p>
 * Initially, the {@code producerBuffer} and {@code toSocketBuffer} are the same
 * but if the socket gets stalled, the {@code producerBuffer} might advance.
 */
final class MultiBufferedSocket<T extends NetworkContext<T>> {

    // Todo: Make BUFFER_CAPACITY configurable. Smaller buffers will yield higher locality

    // Todo: If we consumed and delivered everything. Reset to improve locality. Or perhaps not...

    // Messages:
    // Bytes      Meaning
    // 4          Message length
    // 1          WireType

    private static final int DEFAULT_MAX_MESSAGE_SIZE = 1 << 24; // 16M
    private static final int BUFFER_CAPACITY = DEFAULT_MAX_MESSAGE_SIZE * 2;

    private final T nc;
    private final ISocketChannel sc;
    private final int size;
    private final Buffer[] fromSocketBuffers;
    private final Buffer[] toSocketBuffers;

    /**
     * Index to a Buffer being used for
     * reading from the socket.
     */
    private int fromSocketBuffer;
    /**
     * Index to a Buffer being used for
     * consuming messages previously
     * read from a socket.
     */
    private int consumerBuffer;
    /**
     * Index to a Buffer being used to receive
     * produced messages later to
     * be written to the socket.
     */
    private int producerBuffer;
    /**
     * Index to the Buffer being used for
     * writing to the socket.
     */
    private int toSocketBuffer;

    MultiBufferedSocket(@NotNull final T nc,
                        @NotNull final ISocketChannel sc,
                        final int size) {
        this.nc = nc;
        this.sc = sc;
        if (size < 2)
            throw new IllegalArgumentException("Minimum size is 2 but was " + size);
        this.size = size;
        this.fromSocketBuffers = createBuffers(size, BUFFER_CAPACITY);
        this.toSocketBuffers = createBuffers(size, BUFFER_CAPACITY);
    }

    /**
     * Attempts to read an unspecified amount of bytes from the socket
     * into internal buffers and returns the number of bytes actually read or:
     * <p>
     * Integer.MAX_VALUE if there is no space left in the internal buffers
     * -1 if there was a socket end-of-stream
     *
     * @return the number of bytes read or Integer.MAX_VALUE if no space left in internal
     * buffers or -1 if there was a socket end-of-stream.
     */
    public int readFromSocket() throws IOException {
        final Buffer fromSocketBuffer = fromSocketBuffers[this.fromSocketBuffer];
        if (isBumpRead(fromSocketBuffer)) {
            final int next = next(this.fromSocketBuffer);
            if (next == consumerBuffer)
                // Currently, we cannot read into a Buffer that
                // is being consumed
                return 0;
        }
        final int read = sc.read(fromSocketBuffer.byteBuffer());
        // Update readPosition() ?

        return read;
    }

    /**
     * Attempts to write an unspecified amount of bytes from the
     * internal buffers to the socket and returns the number of bytes actually read or:
     * <p>
     * Integer.MAX_VALUE if there is no space left in the internal buffers
     * -1 if there was a socket end-of-stream
     *
     * @return the number of bytes read or Integer.MAX_VALUE if no space left in internal
     * buffers or -1 if there was a socket end-of-stream.
     */
    public int writeToSocket() throws IOException {
        throw new UnsupportedOperationException();
    }

    /**
     * Invokes the provided {@code tcpHandler} with relevant internal
     * buffers.
     *
     * @param tcpHandler to invoke
     */
    void handleTcp(@NotNull final TcpHandler<T> tcpHandler) {

    }

    private boolean isBumpRead(Buffer buffer) {
        return buffer.bytes.readRemaining() <= DEFAULT_MAX_MESSAGE_SIZE;
    }

    private int next(int bufferIndex) {
        return bufferIndex % size;
    }


/*    private boolean bumpReadFromSocket(Buffer buffer) {
        if (buffer.hasCapacityChanged()) {
            buffer.updateCapacity();
            return true;
        }
        return false;
    }*/

    private Buffer[] createBuffers(final int size,
                                   final int capacity) {
        return IntStream.range(0, size)
                .mapToObj(i -> new Buffer(capacity))
                .toArray(Buffer[]::new);
    }

    private static final class Buffer {

        //private final VanillaBytes<Void> bytes;
        private final Bytes<ByteBuffer> bytes;

        //private long lastCapacity;

        public Buffer(final int capacity) {
            bytes = Bytes.elasticByteBuffer(capacity, capacity);
        }

        public Bytes<ByteBuffer> bytes() {
            return bytes;
        }

        public ByteBuffer byteBuffer() {
            return bytes.underlyingObject();
        }

/*        public boolean hasCapacityChanged() {
            return lastCapacity != bytes.capacity();
        }

        public void updateCapacity() {
            lastCapacity = bytes.capacity();
        }*/

    }

}