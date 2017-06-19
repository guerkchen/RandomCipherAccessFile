import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;

public class RandomChiperAccessFileBlocked implements Closeable{

	
	private final RandomAccessFile randomAccessFile;
	private final Cipher encryptCipher; // verschluesseln
	private final Cipher decryptCipher; // entschluesseln
	private final int blockSize;

	public RandomChiperAccessFileBlocked(File file, String mode, Cipher encryptCipher, Cipher decryptCipher,
			int blockSize) throws FileNotFoundException {

		if (encryptCipher == null && mode.contains("w")) {
			throw new IllegalArgumentException("need encrypt cipher to write to data");
		}
		if (decryptCipher == null && mode.contains("r")) {
			throw new IllegalArgumentException("need decrypt cipher to read from data");
		}

		this.randomAccessFile = new RandomAccessFile(file, mode);

		try {
			if (randomAccessFile.length() % blockSize != 0) {
				throw new IllegalArgumentException("file length must be multiple of blockSize");
			}
		} catch (IOException ioe) {
			throw new FileNotFoundException("given file is no valid file");
		}

		this.encryptCipher = encryptCipher;
		this.decryptCipher = decryptCipher;
		this.blockSize = blockSize;
	}

	public byte[] readBlocks(int firstBlock, int blockCount)
			throws IOException, IllegalBlockSizeException, BadPaddingException {

		// Check, if enough data is there
		if ((long) (firstBlock + blockCount + 1) * (long) blockSize - 1 > randomAccessFile.length()) {
			throw new IOException("try to read unallocated space");
		}

		// set offset
		randomAccessFile.seek(firstBlock * blockSize);

		// read
		byte[] b = new byte[blockCount * blockSize];
		randomAccessFile.read(b);

		// decrypt
		return decryptCipher.doFinal(b);
	}

	public void writeBlocks(int firstBlock, int blockCount, byte[] b)
			throws IOException, IllegalBlockSizeException, BadPaddingException {

		if (b.length != blockCount * blockSize) {
			throw new IllegalArgumentException("b is to small or to big");
		}

		// Check, if new space must be allocated
		long unallocatedSpace = ((long) (firstBlock + blockCount + 1) * (long) blockSize - 1)
				- randomAccessFile.length();
		assert (unallocatedSpace % blockSize == 0);
		if (unallocatedSpace > 0) {
			allocBlocks((int) (unallocatedSpace % blockSize));
		}

		// encrypt
		b = encryptCipher.doFinal(b);

		// set offset
		randomAccessFile.seek(firstBlock * blockSize);

		// write
		randomAccessFile.write(b);
		assert (randomAccessFile.length() % blockSize == 0);
	}

	public void allocBlocks(int blocks) throws IOException, IllegalBlockSizeException, BadPaddingException {
		// alloc data
		byte[] b = new byte[blocks * blockSize];

		// encrypt
		b = encryptCipher.doFinal(b);

		// go to end of file
		randomAccessFile.seek(randomAccessFile.getFilePointer());

		// write
		randomAccessFile.write(b);
		assert (randomAccessFile.length() % blockSize == 0);

	}

	public int allocatedBlocks() throws IOException {
		return (int) (randomAccessFile.length() / blockSize);
	}
	
	public void close() throws IOException {
		randomAccessFile.close();		
	}
}
