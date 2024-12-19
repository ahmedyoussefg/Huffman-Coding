package org.huffman;

import java.util.Arrays;

public class ByteArrayWrapper {
    private byte[] data;
    private int myHashCode;
    public ByteArrayWrapper() {

    }
    public ByteArrayWrapper(byte[] data) {
        this.data = data;
        this.myHashCode = Arrays.hashCode(this.data);
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
        this.myHashCode = Arrays.hashCode(this.data);
    }

    @Override
    public int hashCode(){
        return this.myHashCode;
    }

    @Override
    public boolean equals(Object other){
        if (!(other instanceof ByteArrayWrapper))
            return false;
        return Arrays.equals(this.data, ((ByteArrayWrapper) other).data);
    }

}
