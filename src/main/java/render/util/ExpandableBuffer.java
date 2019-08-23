package render.util;

import org.lwjgl.system.MemoryUtil;

import java.nio.*;

public final class ExpandableBuffer implements Comparable<ExpandableBuffer>, Freeable {

    private ByteBuffer delegate;
    private boolean warningMessage = true;
    
    public ExpandableBuffer(int initialCapacity) {
        delegate = MemoryUtil.memAlloc(initialCapacity);
    }
    
    public ExpandableBuffer(ByteBuffer initialBuffer) {
        this.delegate = initialBuffer;
    }
    
    @Override
    public void free() {
        MemoryUtil.memFree(delegate);
    }
    
    public ExpandableBuffer setWarningMessage(boolean warningMessage) {
        this.warningMessage = warningMessage;
        return this;
    }
    
    private void ensureAdditionalCapacity(int bytesToWrite) {
        if (delegate.remaining() < bytesToWrite) {
            int capacity = delegate.capacity();
            do {
                capacity *= 2;
            } while (capacity - delegate.position() < bytesToWrite);

            delegate.flip();
            ByteBuffer newBuf = MemoryUtil.memAlloc(capacity);
            newBuf.put(delegate);

            MemoryUtil.memFree(delegate);
            
            this.delegate = newBuf;

            if (warningMessage)
                System.err.println("Had to expand the ExpandableBuffer to " + capacity + " bytes");
        }
    }

    
    // ===== DELEGATE METHODS =====
    
    public final byte[] array() {
        return delegate.array();
    }

    public final int arrayOffset() {
        return delegate.arrayOffset();
    }
    
    public ByteBuffer asByteBuffer() {
        return delegate.asReadOnlyBuffer();
    }

    public CharBuffer asCharBuffer() {
        return delegate.asCharBuffer().asReadOnlyBuffer();
    }

    public DoubleBuffer asDoubleBuffer() {
        return delegate.asDoubleBuffer().asReadOnlyBuffer();
    }

    public FloatBuffer asFloatBuffer() {
        return delegate.asFloatBuffer().asReadOnlyBuffer();
    }

    public IntBuffer asIntBuffer() {
        return delegate.asIntBuffer().asReadOnlyBuffer();
    }

    public LongBuffer asLongBuffer() {
        return delegate.asLongBuffer().asReadOnlyBuffer();
    }

    public ExpandableBuffer asReadOnlyBuffer() {
        return new ExpandableBuffer(delegate.asReadOnlyBuffer());
    }

    public ShortBuffer asShortBuffer() {
        return delegate.asShortBuffer().asReadOnlyBuffer();
    }

    public final int capacity() {
        return delegate.capacity();
    }

    public final ExpandableBuffer clear() {
        delegate.clear();
        return this;
    }

    public ExpandableBuffer compact() {
        delegate.compact();
        return this;
    }

    @Override
    public int compareTo(ExpandableBuffer that) {
        return delegate.compareTo(that.delegate);
    }

    public ExpandableBuffer duplicate() {
        return new ExpandableBuffer(delegate.duplicate());
    }

    @Override
    public boolean equals(Object ob) {
        if (ob == this) return true;
        if (!(ob instanceof ExpandableBuffer)) return false;
        
        ExpandableBuffer that = (ExpandableBuffer) ob;
        
        return delegate.equals(that.delegate);
    }

    public final ExpandableBuffer flip() {
        delegate.flip();
        return this;
    }

    public byte get() {
        return delegate.get();
    }

    public ByteBuffer get(byte[] dst, int offset, int length) {
        return delegate.get(dst, offset, length);
    }

    public ByteBuffer get(byte[] dst) {
        return delegate.get(dst);
    }

    public byte get(int index) {
        return delegate.get(index);
    }

    public char getChar() {
        return delegate.getChar();
    }

    public char getChar(int index) {
        return delegate.getChar(index);
    }

    public double getDouble() {
        return delegate.getDouble();
    }

    public double getDouble(int index) {
        return delegate.getDouble(index);
    }

    public float getFloat() {
        return delegate.getFloat();
    }

    public float getFloat(int index) {
        return delegate.getFloat(index);
    }

    public int getInt() {
        return delegate.getInt();
    }

    public int getInt(int index) {
        return delegate.getInt(index);
    }

    public long getLong() {
        return delegate.getLong();
    }

    public long getLong(int index) {
        return delegate.getLong(index);
    }

    public short getShort() {
        return delegate.getShort();
    }

    public short getShort(int index) {
        return delegate.getShort(index);
    }

    public final boolean hasArray() {
        return delegate.hasArray();
    }

    public final boolean hasRemaining() {
        return delegate.hasRemaining();
    }

    public int hashCode() {
        return delegate.hashCode();
    }

    public boolean isDirect() {
        return delegate.isDirect();
    }

    public boolean isReadOnly() {
        return delegate.isReadOnly();
    }

    public final int limit() {
        return delegate.limit();
    }

    public final ExpandableBuffer limit(int arg0) {
        delegate.limit(arg0);
        return this;
    }

    public final ExpandableBuffer mark() {
        delegate.mark();
        return this;
    }

    public final ByteOrder order() {
        return delegate.order();
    }

    public final ExpandableBuffer order(ByteOrder bo) {
        delegate.order(bo);
        return this;
    }

    public final int position() {
        return delegate.position();
    }

    public final ExpandableBuffer position(int arg0) {
        delegate.position(arg0);
        return this;
    }

    public ByteBuffer put(byte b) {
        ensureAdditionalCapacity(1);
        return delegate.put(b);
    }

    public ByteBuffer put(byte[] src, int offset, int length) {
        ensureAdditionalCapacity(Math.min(src.length - offset, length));
        return delegate.put(src, offset, length);
    }

    public final ByteBuffer put(byte[] src) {
        ensureAdditionalCapacity(src.length);
        return delegate.put(src);
    }

    public ByteBuffer put(ByteBuffer src) {
        ensureAdditionalCapacity(src.remaining());
        return delegate.put(src);
    }

    public ByteBuffer put(int index, byte b) {
        ensureAdditionalCapacity(1);
        return delegate.put(index, b);
    }

    public ByteBuffer putChar(char value) {
        ensureAdditionalCapacity(2);
        return delegate.putChar(value);
    }

    public ByteBuffer putChar(int index, char value) {
        ensureAdditionalCapacity(2);
        return delegate.putChar(index, value);
    }

    public ByteBuffer putDouble(double value) {
        ensureAdditionalCapacity(8);
        return delegate.putDouble(value);
    }

    public ByteBuffer putDouble(int index, double value) {
        ensureAdditionalCapacity(8);
        return delegate.putDouble(index, value);
    }

    public ByteBuffer putFloat(float value) {
        ensureAdditionalCapacity(4);
        return delegate.putFloat(value);
    }

    public ByteBuffer putFloat(int index, float value) {
        ensureAdditionalCapacity(4);
        return delegate.putFloat(index, value);
    }

    public ByteBuffer putInt(int index, int value) {
        ensureAdditionalCapacity(4);
        return delegate.putInt(index, value);
    }

    public ByteBuffer putInt(int value) {
        ensureAdditionalCapacity(4);
        return delegate.putInt(value);
    }

    public ByteBuffer putLong(int index, long value) {
        ensureAdditionalCapacity(8);
        return delegate.putLong(index, value);
    }

    public ByteBuffer putLong(long value) {
        ensureAdditionalCapacity(8);
        return delegate.putLong(value);
    }

    public ByteBuffer putShort(int index, short value) {
        ensureAdditionalCapacity(2);
        return delegate.putShort(index, value);
    }

    public ByteBuffer putShort(short value) {
        ensureAdditionalCapacity(2);
        return delegate.putShort(value);
    }

    public final int remaining() {
        return delegate.remaining();
    }

    public final ExpandableBuffer reset() {
        delegate.reset();
        return this;
    }

    public final ExpandableBuffer rewind() {
        delegate.rewind();
        return this;
    }

    public ByteBuffer slice() {
        return delegate.slice();
    }

    public String toString() {
        return delegate.toString();
    }

}
