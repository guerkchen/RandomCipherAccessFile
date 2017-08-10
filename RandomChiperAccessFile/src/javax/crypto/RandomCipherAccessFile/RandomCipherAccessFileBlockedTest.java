import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.Random;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.junit.Test;

class RandomCipherAccessFileBlockedTest {

	@Test
	public void readWriteTest() {
		final int blockSize = 16;
		Random rand = new Random();
		byte[] password = new byte[16];
		rand.nextBytes(password);

		try {
			SecretKey key = new SecretKeySpec(password, "AES");

			Cipher encryptCipher = Cipher.getInstance("AES/ECB/NoPadding");
			encryptCipher.init(Cipher.ENCRYPT_MODE, key);

			Cipher decryptCipher = Cipher.getInstance("AES/ECB/NoPadding");
			decryptCipher.init(Cipher.DECRYPT_MODE, key);

			File file = new File("test");
			file.delete();

			RandomChiperAccessFileBlocked randomChiperAccessFileBlocked = new RandomChiperAccessFileBlocked(file, "rw",
					encryptCipher, decryptCipher, blockSize);

			int testBlocks = 10;
			randomChiperAccessFileBlocked.allocBlocks(testBlocks);

			byte[] data = new byte[blockSize * 9];
			rand.nextBytes(data);
			randomChiperAccessFileBlocked.writeBlocks(1, 9, data);

			byte[] data2 = randomChiperAccessFileBlocked.readBlocks(1, 9);

			assertEquals(10, randomChiperAccessFileBlocked.allocatedBlocks());
			assertArrayEquals(data, data2);

			randomChiperAccessFileBlocked.close();
		} catch (Exception e) {
			e.printStackTrace();
			fail("Exception");
		}
	}

	@Test
	public void AllocOnWrite() {
		final int blockSize = 16;
		Random rand = new Random();
		byte[] password = new byte[16];
		rand.nextBytes(password);

		try {
			SecretKey key = new SecretKeySpec(password, "AES");

			Cipher encryptCipher = Cipher.getInstance("AES/ECB/NoPadding");
			encryptCipher.init(Cipher.ENCRYPT_MODE, key);

			Cipher decryptCipher = Cipher.getInstance("AES/ECB/NoPadding");
			decryptCipher.init(Cipher.DECRYPT_MODE, key);

			File file = new File("test");
			file.delete();

			RandomChiperAccessFileBlocked randomChiperAccessFileBlocked = new RandomChiperAccessFileBlocked(file, "rw",
					encryptCipher, decryptCipher, blockSize);

			byte[] data = new byte[blockSize * 9];
			rand.nextBytes(data);
			randomChiperAccessFileBlocked.writeBlocks(1, 9, data);

			byte[] data2 = randomChiperAccessFileBlocked.readBlocks(1, 9);

			assertEquals(10, randomChiperAccessFileBlocked.allocatedBlocks());
			assertArrayEquals(data, data2);

			randomChiperAccessFileBlocked.close();
		} catch (Exception e) {
			e.printStackTrace();
			fail("Exception");
		}
	}

	@Test
	public void OverrideSomething() {
		final int blockSize = 16;
		Random rand = new Random();
		byte[] password = new byte[16];
		rand.nextBytes(password);

		try {
			SecretKey key = new SecretKeySpec(password, "AES");

			Cipher encryptCipher = Cipher.getInstance("AES/ECB/NoPadding");
			encryptCipher.init(Cipher.ENCRYPT_MODE, key);

			Cipher decryptCipher = Cipher.getInstance("AES/ECB/NoPadding");
			decryptCipher.init(Cipher.DECRYPT_MODE, key);

			File file = new File("test");
			file.delete();

			RandomChiperAccessFileBlocked randomChiperAccessFileBlocked = new RandomChiperAccessFileBlocked(file, "rw",
					encryptCipher, decryptCipher, blockSize);

			byte[] data1 = new byte[blockSize * 10];
			rand.nextBytes(data1);
			randomChiperAccessFileBlocked.writeBlocks(0, 10, data1);

			byte[] data2 = new byte[blockSize * 10];
			rand.nextBytes(data2);
			randomChiperAccessFileBlocked.writeBlocks(5, 10, data2);

			byte[] compare = new byte[blockSize * 15];
			for (int block = 0; block < 15; block++) {
				for (int i = 0; i < blockSize; i++) {
					if (block < 5) {
						compare[block * blockSize + i] = data1[block * blockSize + i];
					} else {
						compare[block * blockSize + i] = data2[(block - 5) * blockSize + i];
					}
				}
			}

			byte[] data3 = randomChiperAccessFileBlocked.readBlocks(0, 15);

			assertEquals(15, randomChiperAccessFileBlocked.allocatedBlocks());
			assertArrayEquals(compare, data3);

			randomChiperAccessFileBlocked.close();
		} catch (Exception e) {
			e.printStackTrace();
			fail("Exception");
		}
	}

	@Test
	public void IllegalRead() {
		final int blockSize = 16;

		Random rand = new Random();
		byte[] password = new byte[16];
		rand.nextBytes(password);

		try {
			SecretKey key = new SecretKeySpec(password, "AES");

			Cipher encryptCipher = Cipher.getInstance("AES/ECB/NoPadding");
			encryptCipher.init(Cipher.ENCRYPT_MODE, key);

			Cipher decryptCipher = Cipher.getInstance("AES/ECB/NoPadding");
			decryptCipher.init(Cipher.DECRYPT_MODE, key);

			File file = new File("test");
			file.delete();

			RandomChiperAccessFileBlocked randomChiperAccessFileBlocked = new RandomChiperAccessFileBlocked(file, "rw",
					encryptCipher, decryptCipher, blockSize);

			byte[] data = new byte[blockSize * 5];
			rand.nextBytes(data);
			randomChiperAccessFileBlocked.writeBlocks(1, 5, data);

			try {
				randomChiperAccessFileBlocked.readBlocks(1, 6);
				fail("No Exception");
			} catch (IOException ioe) {
			}

			randomChiperAccessFileBlocked.close();
		} catch (Exception e) {
			e.printStackTrace();
			fail("Exception");
		}
	}

}
