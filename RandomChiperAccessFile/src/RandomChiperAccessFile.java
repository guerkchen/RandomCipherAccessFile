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
		byte[] b = new byte[1];
		readFully(b);
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
			return randomcipherAccessFileManaged.read(offset, length);
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
		// TODO Auto-generated method stub
		return null;
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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int readUnsignedByte() throws IOException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int readUnsignedShort() throws IOException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int skipBytes(int arg0) throws IOException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void write(int arg0) throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public void write(byte[] b) throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public void write(byte[] arg0, int arg1, int arg2) throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public void writeBoolean(boolean arg0) throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public void writeByte(int arg0) throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public void writeBytes(String arg0) throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public void writeChar(int arg0) throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public void writeChars(String arg0) throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public void writeDouble(double arg0) throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public void writeFloat(float arg0) throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public void writeInt(int arg0) throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public void writeLong(long arg0) throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public void writeShort(int arg0) throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public void writeUTF(String arg0) throws IOException {
		// TODO Auto-generated method stub

	}

	public void seek(long pos) {
		// TODO Auto-generated method stub

	}

	public long getFilePointer() {
		// TODO Auto-generated method stub
		return 0;
	}

	private ByteBuffer readInByteBuffer(int bytes) throws IOException {
		byte[] b = new byte[bytes];
		readFully(b);
		return ByteBuffer.wrap(b);
	}
}