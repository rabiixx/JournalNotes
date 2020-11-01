package journal;
/**
 *
 * @author MAZ
 */
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

//
public final class ReadNote {
  
    static private final String CLASS_NAME = ReadNote.class.getName();
    static private final Logger LOGGER = Logger.getLogger(CLASS_NAME);
    
    static private final int ITERATIONS = 2048;
  
    private String operate (final int n) {
    
        try {
                        
            final String journalFile = System.getProperty("user.dir") + File.separator +
                    "data" + File.separator +
                    "journal.data";
            final File file = new File(journalFile);
            
            try (final Scanner is = new Scanner(file)) {
                
                String note = "";
                for (int notes = 0; notes < n; ++notes) {
                    note = is.nextLine();
                }
                
                String[] parts = note.split(":");
                
                final byte[] encodedParams = Base64.getDecoder().decode( parts[1] );
                
                System.out.print("Introduce contraseña para desencriptar la nota: ");
                final char[] passwd = System.console().readPassword();
                
                AESCipherGenerator acg = new AESCipherGenerator();
                
                Cipher cipher = acg.getDecrypter(passwd, encodedParams);

                byte[] decodedBytes = Base64.getDecoder().decode( parts[0] );
                byte[] decryptedMsg = cipher.doFinal( decodedBytes );
                
                String res = new String( decryptedMsg );
                return res;
                
            } catch (final IOException |
                    NoSuchElementException ex) {
                LOGGER.log(Level.SEVERE, ex.getMessage(), ex.getCause());
                return "";
            }
            
        } catch (GeneralSecurityException ex) {
            ex.printStackTrace();
            LOGGER.log(Level.SEVERE, ex.getMessage(), ex.getCause());
        }

        return "";
        
    }
  
    public String read(final int n) {

        return AccessController.doPrivileged(new PrivilegedAction<String>(){
            
            @Override
            public String run() {
                try {
                    return operate( n );    
                } catch (AccessControlException ex ) {
                    LOGGER.log(Level.SEVERE, ex.getMessage(), ex.getCause());
                    return "";
                }
            }
        });
    }
    
}