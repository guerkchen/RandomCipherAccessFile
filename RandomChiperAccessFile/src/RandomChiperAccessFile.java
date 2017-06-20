import java.io.Closeable;
import java.io.DataInput;
import java.io.DataOutput;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;

public class RandomChiperAccessFile implements DataOutput, DataInput, Closeable {
	private static final int MIN_SIZE_FOR_CRYPTION = 4;
	private static final ByteOrder BYTE_ORDER = ByteOrder.BIG_ENDIAN;

	RandomCipherAccessFileManaged randomcipherAccessFileManaged;

	private long currentSeek;

	public RandomChiperAccessFile(File file, String mode, Cipher encryptCipher, Cipher decryptCipher)
			throws FileNotFoundException {
		if (encryptCipher == null && mode.contains("w")) {
			throw new IllegalArgumentException("Need encrypt cipher to write to data");
		}
		if (decryptCipher == null && mode.contains("r")) {
			throw new IllegalArgumentException("Need decrypt cipher to read from data");
		}

		randomcipherAccessFileManaged = new RandomCipherAccessFileManaged(file, mode, encryptCipher, decryptCipher,
				MIN_SIZE_FOR_CRYPTION);
		
		currentSeek = 0;
	}

	public RandomChiperAccessFile(String file, String mode, Cipher encryptCipher, Cipher decryptCipher)
			throws FileNotFoundException {
		this(new File(file), mode, encryptCipher, decryptCipher);
	}

	@Override
	public void close() throws IOException {
		randomcipherAccessFileManaged.close();
	}

	@Override
	public boolean readBoolean() throws IOException {
		byte b = readByte();
		return (b != 0);
	}

	@Override
	public byte readByte() throws IOException {		
		byte[] b = readFully(1);
		return b[0];
	}

	@Override
	public char readChar() throws IOException {
		return (char) readByte();
	}

	@Override
	public double readDouble() throws IOException {
		final ByteBuffer bb = readInByteBuffer(Double.BYTES);
		bb.order(BYTE_ORDER);
		return bb.getDouble();
	}

	@Override
	public float readFloat() throws IOException {
		final ByteBuffer bb = readInByteBuffer(Float.BYTES);
		bb.order(BYTE_ORDER);
		return bb.getFloat();
	}

	@Override
	public void readFully(byte[] b) throws IOException {
		byte[] read = readFully(currentSeek, b.length);

		for (int i = 0; i < b.length; i++) {
			b[i] = read[i];
		}
	}

	@Override
	public void readFully(byte[] b, int offset, int length) throws IOException {
		byte[] read = readFully(offset, length);

		for (int i = 0; i < length && i < b.length; i++) {
			b[i] = read[i];
		}

	}

	public byte[] readFully(int length) throws IOException {
		return readFully(currentSeek, length);
	}

	public byte[] readFully(long offset, int length) throws IOException {
		try {
			byte[] b =  randomcipherAccessFileManaged.read(offset, length);
			currentSeek += length;
			return b;
		} catch (IllegalBlockSizeException | BadPaddingException e) {
			throw new IOException(e.getMessage());
		}
	}

	@Override
	public int readInt() throws IOException {
		final ByteBuffer bb = readInByteBuffer(Integer.BYTES);
		bb.order(BYTE_ORDER);
		return bb.getInt();
	}

	@Override
	public String readLine() throws IOException {
		String s = "";
		
		char c;
		do{
			c = (char) readFully(currentSeek, 1)[0];
			s += c;
		} while(c != '\n');
		
		return s;
	}

	@Override
	public long readLong() throws IOException {
		final ByteBuffer bb = readInByteBuffer(Long.BYTES);
		bb.order(BYTE_ORDER);
		return bb.getLong();
	}

	@Override
	public short readShort() throws IOException {
		final ByteBuffer bb = readInByteBuffer(Short.BYTES);
		bb.order(BYTE_ORDER);
		return bb.getShort();
	}

	@Override
	public String readUTF() throws IOException {
		throw new IllegalStateException("Not implemented yet");
	}

	@Override
	public int readUnsignedByte() throws IOException {
		byte b = readByte();
		return (b & 0xff);
	}

	@Override
	public int readUnsignedShort() throws IOException {
		short b = readShort();
		return (b & 0xff);
	}

	@Override
	public int skipBytes(int n) throws IOException {
		currentSeek += n;
		return n;
	}

	@Override
	public void write(int v) throws IOException {
		byte[] b = new byte[1];
		b[0] = (byte) v;
		
		write(b, currentSeek, 1);
	}

	@Override
	public void write(byte[] b) throws IOException {
		write(b, currentSeek, b.length);
	}

	@Override
	public void write(byte[] b, int offset, int length) throws IOException {
		write(b, offset, length);
	}
	
	public void write(byte[] b, long offset, int length) throws IOException{
		try {
			randomcipherAccessFileManaged.write(offset, length, b);
			currentSeek += length;
		} catch (IllegalBlockSizeException | BadPaddingException e) {
			throw new IOException(e.getMessage());
		}
	}

	@Override
	public void writeBoolean(boolean v) throws IOException {
		if(v){
			writeByte(1);
		} else {
			writeByte(0);
		}
	}

	@Override
	public void writeByte(int v) throws IOException {
		writeByte((byte) v);
	}

	@Override
	public void writeBytes(String s) throws IOException {
		for(byte c : s.getBytes()){
			writeChar(c);
		}
	}

	@Override
	public void writeChar(int v) throws IOException {
		writeByte((byte) v);
	}

	@Override
	public void writeChars(String s) throws IOException {
		for(byte c : s.getBytes()){
			writeChar(c);
		}
	}

	@Override
	public void writeDouble(double v) throws IOException {
		final ByteBuffer bb = ByteBuffer.allocate(Double.BYTES);
		bb.order(BYTE_ORDER);
		bb.putDouble(v);
		
		write(bb.array());
	}

	@Override
	public void writeFloat(float v) throws IOException {
		final ByteBuffer bb = ByteBuffer.allocate(Float.BYTES);
		bb.order(BYTE_ORDER);
		bb.putFloat(v);
		
		write(bb.array());
	}

	@Override
	public void writeInt(int v) throws IOException {
		final ByteBuffer bb = ByteBuffer.allocate(Integer.BYTES);
		bb.order(BYTE_ORDER);
		bb.putInt(v);
		
		write(bb.array());

	}

	@Override
	public void writeLong(long v) throws IOException {
		final ByteBuffer bb = ByteBuffer.allocate(Long.BYTES);
		bb.order(BYTE_ORDER);
		bb.putLong(v);
		
		write(bb.array());
	}

	@Override
	public void writeShort(int v) throws IOException {
		final ByteBuffer bb = ByteBuffer.allocate(Short.BYTES);
		bb.order(BYTE_ORDER);
		bb.putShort((short) v);
		
		write(bb.array());
	}

	@Override
	public void writeUTF(String s) throws IOException {
		write(s.getBytes("UTF-8"));
	}

	public void seek(long pos) {
		currentSeek = pos;
	}

	public long getFilePointer() {
		return currentSeek;
	}

	private ByteBuffer readInByteBuffer(int bytes) throws IOException {
		byte[] b = new byte[bytes];
		readFully(b);
		return ByteBuffer.wrap(b);
	}
}