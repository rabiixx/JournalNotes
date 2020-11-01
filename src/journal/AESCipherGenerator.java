package journal;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.security.AlgorithmParameters;
import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;
import javax.crypto.spec.SecretKeySpec;


final class AESCipherGenerator {
  
    static private final String CLASS_NAME = AESCipherGenerator.class.getName();
    static private final Logger LOGGER = Logger.getLogger(CLASS_NAME);  

    static private final String BLOCK_CIPHER = "AES";
    static private final String PBE_BLOCK_CIPHER = "PBEWithHmacSHA256AndAES_128";  
    static private final int KEY_LENGTH  = 16;        /* Bytes - 128bit key */
    static private final int SALT_LENGTH = 64;

    private final String operationMode;
    private final String paddingScheme;
    private final String cipherTransform;
    private final String pbeCipherTransform;
    private final MessageDigest md;
    private final SecureRandom rg;
  
    static byte[] toBytes (final char[] chars) {
        final CharBuffer charBuffer = CharBuffer.wrap(chars);
        final ByteBuffer byteBuffer = Charset.forName("UTF-8").encode(charBuffer);
        final byte[] bytes = Arrays.copyOfRange(byteBuffer.array(),
            byteBuffer.position(), byteBuffer.limit());
        return bytes;
    }  

    AESCipherGenerator (final String mode, final String padding) throws GeneralSecurityException {
        try {
            this.operationMode = mode;
            this.paddingScheme = padding;
            this.cipherTransform = BLOCK_CIPHER + "/" + operationMode + "/" + paddingScheme;
            this.pbeCipherTransform = PBE_BLOCK_CIPHER + "/CBC/PKCS5Padding";
            this.md = MessageDigest.getInstance("SHA-256");
            this.rg = new SecureRandom();
        } catch (final NoSuchAlgorithmException ex) {
            LOGGER.log(Level.SEVERE, "algoritmo SHA-256 no disponible", ex.getCause());
            throw new GeneralSecurityException();
        }
    }
  
    AESCipherGenerator () throws GeneralSecurityException {
        this("CBC", "PKCS5Padding");
    }

    Cipher getEncrypter (final char[] passwd, final int iterations) throws GeneralSecurityException {
        if (iterations > 0)
            return getCipher(Cipher.ENCRYPT_MODE, passwd, iterations);
        else
            throw new IllegalArgumentException("Invalid value for number of iterations");
    }

    Cipher getDecrypter (final char[] passwd, final int iterations) throws GeneralSecurityException {
        if (iterations > 0)
            return getCipher(Cipher.DECRYPT_MODE, passwd, iterations);
        else
            throw new IllegalArgumentException("Invalid value for number of iterations");
    }
  
    Cipher getEncrypter (final char[] passwd,
                         final byte[] encodedParams) throws GeneralSecurityException, IOException {
        return getCipher(Cipher.ENCRYPT_MODE, passwd, encodedParams);
    }

    Cipher getDecrypter (final char[] passwd,
                         final byte[] encodedParams) throws GeneralSecurityException, IOException {
        return getCipher(Cipher.DECRYPT_MODE, passwd, encodedParams);
    }  
  
    Cipher getEncrypter (final byte[] bytes) throws GeneralSecurityException {
        return getCipher(Cipher.ENCRYPT_MODE, bytes);
    }

    Cipher getDecrypter (final byte[] bytes) throws GeneralSecurityException {
        return getCipher(Cipher.DECRYPT_MODE, bytes);
    }

    private Cipher getCipher (final int mode,
                              final byte[] rawMaterial) throws GeneralSecurityException {
    
    try {

      // Generación del resumen digital
      /* Generate given password hash (digest) using SHA-256 */
      final byte[] bytes = md.digest(rawMaterial);

      /** 
        * Key can be obtained through key generators ( KeyGenerator ), 
        * key specification ( KeySpec ) using key factory ( KeyFactory )
        * or cetificates. In this case we are getting key using the 
        * second method.
        */

      // Preparación de clave simétrica
      /** 
        * We need to indicate the algorithm for which
        * the generated key will be used, in this case AES
        */
      final SecretKeySpec key = new SecretKeySpec(bytes, 0, KEY_LENGTH, BLOCK_CIPHER);


      // Instanciaciçon y configuración del cifrador
      final Cipher cipher = Cipher.getInstance(cipherTransform);
      
      /** 
        * In this case we are using AES with CBC mode operation and
        * PKCS5 padding scheme: AES/CBC/PKCS5Padding 
        */
      if (operationMode.compareTo("ECB") != 0) {
        
        /** 
          * If we are using Cipher Block Chaining (CBC) we need to define a initialization
          * vector that will be used in the first CBC iteration/round to ensure the 
          * randomness of the encryption process.
          */
        final IvParameterSpec iv = new IvParameterSpec(bytes, KEY_LENGTH, KEY_LENGTH);
        
        /* cypher( Encrypt Mode, Derived Key, Initialization Vector) */
        cipher.init(mode, key, iv);
      
      } else {
        cipher.init(mode, key);
      }

      // Borrado de datos sensibles
      Arrays.fill(rawMaterial, (byte) 0);
      Arrays.fill(bytes, (byte) 0);

      return cipher;

    } catch (final NoSuchAlgorithmException |
                   NoSuchPaddingException |
                   InvalidAlgorithmParameterException |
                   InvalidKeyException ex) {
      LOGGER.log(Level.SEVERE, ex.getMessage(), ex.getCause());
      throw new GeneralSecurityException();
    }
    
  }
  
    private Cipher getCipher (final int mode,
                              final char[] password,
                              final int iterations) throws GeneralSecurityException {
    try {
      
        // Generación de clave secreta a partir de contraseña
        final PBEKeySpec keySpec = new PBEKeySpec(password);  
        final SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(PBE_BLOCK_CIPHER);
        final SecretKey key = keyFactory.generateSecret(keySpec);
        keySpec.clearPassword(); // Se borra el duplicado interno de la contraseña

        // Borrado de contraseña
        Arrays.fill(password, (char) 0);      

        // Generación del vector de inicialización
        final byte[] ivRawData = new byte[KEY_LENGTH];
        rg.nextBytes(ivRawData);
        final IvParameterSpec iv = new IvParameterSpec(ivRawData);

        // Parámetros de configuración del cifrador PBE
        final byte[] salt = new byte[SALT_LENGTH];
        rg.nextBytes(salt);
        final PBEParameterSpec params = new PBEParameterSpec(salt, iterations, iv);

        // Instanciación del cifrador PBE
        final Cipher cipher = Cipher.getInstance(pbeCipherTransform);

        // Configuración del cifrador PBE
        cipher.init(mode, key, params);

        return cipher;

    } catch (final NoSuchAlgorithmException | 
                   NoSuchPaddingException |
                   InvalidKeySpecException |
                   InvalidKeyException |
                   InvalidAlgorithmParameterException ex) {
       
        System.out.println("aqui ");
        LOGGER.log(Level.SEVERE, ex.getMessage(), ex.getCause());
        throw new GeneralSecurityException();
    }

  }

    private Cipher getCipher (final int mode,
                            final char[] password,
                            final byte[] encodedParams) throws GeneralSecurityException, IOException {
    
        try {

            // Reconstrucción de parámetros del cifrador PBE
            final AlgorithmParameters algParams = AlgorithmParameters.getInstance(PBE_BLOCK_CIPHER);
            algParams.init(encodedParams);

            final PBEParameterSpec params = algParams.getParameterSpec(PBEParameterSpec.class);

            // Generación de clave secreta a partir de contraseña
            final PBEKeySpec keySpec = new PBEKeySpec(password);
            final SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(PBE_BLOCK_CIPHER);
            final SecretKey key = keyFactory.generateSecret(keySpec);
            keySpec.clearPassword(); // Se borra el duplicado interno de la contraseña

            // Borrado de datos sensibles
            Arrays.fill(password, (char) 0);      

            // Instanciación del cifrador PBE
            final Cipher cipher = Cipher.getInstance(pbeCipherTransform);

            // Configuración del cifrador PBE
            cipher.init(mode, key, params);

            return cipher;

        } catch (final NoSuchAlgorithmException |
                       NoSuchPaddingException |
                       InvalidAlgorithmParameterException ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage(), ex.getCause());
            throw new GeneralSecurityException();
        } catch (final IOException ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage(), ex.getCause());
            throw new IOException();
        }    
    }  
}