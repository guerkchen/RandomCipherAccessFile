import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;

public class RandomCipherAccessFileManaged implements Closeable{
	
	
	private final RandomChiperAccessFileBlocked randomCipherAccessFileBlocked;
	private final int blockSize;
	private final String mode;

	public RandomCipherAccessFileManaged(File file, String mode, Cipher encryptCipher, Cipher decryptCipher,
			int blockSize) throws FileNotFoundException {
		if (encryptCipher == null && mode.contains("w")) {
			throw new IllegalArgumentException("Need encrypt cipher to write to data");
		}
		if (decryptCipher == null && mode.contains("r")) {
			throw new IllegalArgumentException("Need decrypt cipher to read from data");
		}

		randomCipherAccessFileBlocked = new RandomChiperAccessFileBlocked(file, mode, encryptCipher, decryptCipher,
				blockSize);
		this.blockSize = blockSize;
		this.mode = mode;
	}

	public byte[] read(long offset, int length) throws IllegalBlockSizeException, BadPaddingException, IOException {
		if(!mode.contains("r")){
			throw new IllegalStateException("file not openened in read mode");
		}
		
		int firstBlock = (int) (offset / blockSize);
		int lastBlock = (int) ((offset + length) / blockSize);
		int blockCount = lastBlock - firstBlock + 1;

		// Read blocks
		byte[] b = randomCipherAccessFileBlocked.readBlocks(firstBlock, blockCount);

		// Cut data (if necessary)
		int offsetBegin = (int) (offset % blockSize);
		if (!(offsetBegin == 0 && length % blockSize == 0)) {
			return Arrays.copyOfRange(b, offsetBegin, offsetBegin + length);
		} else {
			return b;
		}
	}

	public void write(long offset, int length, byte[] b) throws IllegalBlockSizeException, BadPaddingException, IOException{
		if(!mode.contains("w")){
			throw new IllegalStateException("file not openened in write mode");
		}
		
		int firstBlock = (int) (offset / blockSize);
		int lastBlock = (int) ((offset + length) / blockSize);
		int blockCount = lastBlock - firstBlock + 1;
		
		int offsetBegin = (int) (offset % blockSize);
		if(offsetBegin != 0){
			// padding at the begin
			byte[] firstBlockBytes = randomCipherAccessFileBlocked.readBlocks(firstBlock, 1);
			b = concat(Arrays.copyOfRange(firstBlockBytes, 0, offsetBegin), b);
		}
		
		int offsetEnd = (offsetBegin + length) % blockSize;
		if(offsetEnd != 0){
			// padding at the end
			byte[] lastBlockBytes = randomCipherAccessFileBlocked.readBlocks(lastBlock, 1);
			b = concat(b, Arrays.copyOfRange(lastBlockBytes, offsetEnd, blockSize - offsetEnd));
		}
		
		randomCipherAccessFileBlocked.writeBlocks(firstBlock, blockCount, b);		
	}

	public byte[] concat(byte[] a, byte[] b) {
		int aLen = a.length;
		int bLen = b.length;
		byte[] c = new byte[aLen + bLen];
		System.arraycopy(a, 0, c, 0, aLen);
		System.arraycopy(b, 0, c, aLen, bLen);
		return c;
	}
	
	public long length() throws IOException{
		return randomCipherAccessFileBlocked.allocatedBlocks() * blockSize;
	}
	
	public void close() throws IOException {
		randomCipherAccessFileBlocked.close();		
	}
}
