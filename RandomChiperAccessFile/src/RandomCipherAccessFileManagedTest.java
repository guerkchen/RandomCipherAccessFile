import static org.junit.Assert.*;

import java.io.File;
import java.util.Random;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.junit.Test;

public class RandomCipherAccessFileManagedTest {

	@Test
	public void readWriteTest() {
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

			RandomCipherAccessFileManaged randomChiperAccessFileManaged = new RandomCipherAccessFileManaged(file, "rw",
					encryptCipher, decryptCipher, 16);

			byte[] data = new byte[15];
			rand.nextBytes(data);
			randomChiperAccessFileManaged.write(10, 15, data);

			byte[] data2 = randomChiperAccessFileManaged.read(10, 15);

			// assertEquals(25, randomChiperAccessFileManaged.length());
			assertArrayEquals(data, data2);

			randomChiperAccessFileManaged.close();
		} catch (Exception e) {
			e.printStackTrace();
			fail("Exception");
		}
	}

	@Test
	public void OverrideSomething() {
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

			RandomCipherAccessFileManaged randomChiperAccessFileManaged = new RandomCipherAccessFileManaged(file, "rw",
					encryptCipher, decryptCipher, 16);

			byte[] data1 = new byte[100];
			rand.nextBytes(data1);
			randomChiperAccessFileManaged.write(0, 100, data1);

			byte[] data2 = new byte[10];
			rand.nextBytes(data2);
			randomChiperAccessFileManaged.write(10, 10, data2);

			byte[] compare = new byte[100];
			for (int i = 0; i < 100; i++) {
				if(i >= 10 && i < 20){
					compare[i] = data2[i - 10];
				} else {
					compare[i] = data1[i];
				}
			}

			byte[] data3 = randomChiperAccessFileManaged.read(0, 100);

			assertArrayEquals(compare, data3);

			randomChiperAccessFileManaged.close();
		} catch (Exception e) {
			e.printStackTrace();
			fail("Exception");
		}
	}
}
