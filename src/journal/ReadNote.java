package journal;

import java.io.File;
import java.io.IOException;
import java.security.AccessControlException;
import java.security.AccessController;
import java.security.GeneralSecurityException;
import java.security.PrivilegedAction;
import java.util.Base64;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.Cipher;

public final class ReadNote {
  
    static private final String CLASS_NAME = ReadNote.class.getName();
    static private final Logger LOGGER = Logger.getLogger(CLASS_NAME);
      
    private String operate (final int n) {
    
        try {
                        
            final String journalFile = System.getProperty("user.dir") + File.separator +
                    "data" + File.separator +
                    "journal.data";
            final File file = new File(journalFile);
            
            String note;
            try (Scanner is = new Scanner(file)) {
                note = "";
                for (int notes = 0; notes < n; ++notes) {
                    note = is.nextLine();
                }
            } catch ( final IOException | NoSuchElementException ex ) {
                LOGGER.log(Level.SEVERE, ex.getMessage(), ex.getCause());
                System.out.println("test1");
                return "";
            }
            
            String[] parts = note.split(":");

            /* Decodificamos los parametros codificados en base64 */
            final byte[] encodedParams = Base64.getDecoder().decode( parts[1] );

            System.out.print("Introduce contraseña para desencriptar la nota: ");
            final char[] passwd = System.console().readPassword();

            AESCipherGenerator acg = new AESCipherGenerator();

            Cipher cipher = acg.getDecrypter(passwd, encodedParams);

            /* Decodificamos el mensaje encriptado codificado en base64 */
            byte[] decodedBytes = Base64.getDecoder().decode( parts[0] );

            /* Desencriptamos los el mensaje */
            byte[] decryptedMsg = cipher.doFinal( decodedBytes );

            return new String( decryptedMsg );
            
        } catch ( final IOException | GeneralSecurityException | NoSuchElementException ex ) {
            LOGGER.log(Level.SEVERE, ex.getMessage(), ex.getCause());
            return "";
        }
    }
  
    public String read(final int n) {

        return AccessController.doPrivileged((PrivilegedAction<String>) () -> {
            try {
                return operate( n );
            } catch (AccessControlException ex ) {
                LOGGER.log(Level.SEVERE, ex.getMessage(), ex.getCause());
                return "";
            }
        });
    }
    
}