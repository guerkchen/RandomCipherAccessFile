import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;

class RandomChiperAccessFileBlocked implements Closeable{
	/*
	 * Layer 0: Direct Access to the file (through RandomAccessFile)
	 * This class does the cryptographic stuff
	 * This class just allows read/write of blocks, which can be encrypted/decrypted independent from the rest of the data
	 * This class won't check, if the mode (r/w) is correct
	 */
	
	private final RandomAccessFile randomAccessFile;
	private final Cipher encryptCipher; // verschluesseln
	private final Cipher decryptCipher; // entschluesseln
	private final int blockSize;

	public RandomChiperAccessFileBlocked(File file, String mode, Cipher encryptCipher, Cipher decryptCipher,
			int blockSize) throws FileNotFoundException {

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
		
		if(decryptCipher == null){
			throw new IllegalStateException("no decryption cypher specified");
		}

		// Check, if enough data is there
		if ((long) (firstBlock + blockCount) * (long) blockSize > randomAccessFile.length()) {
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
		
		if(encryptCipher == null){
			throw new IllegalStateException("no encryption cypher specified");
		}

		if (b.length != blockCount * blockSize) {
			throw new IllegalArgumentException("b is to small or to big");
		}

		// Check, if new space must be allocated
		long unallocatedSpace = (long) (firstBlock + blockCount + 1) * (long) blockSize
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
		if(encryptCipher == null){
			throw new IllegalStateException("no encryption cypher specified");
		}
		
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
