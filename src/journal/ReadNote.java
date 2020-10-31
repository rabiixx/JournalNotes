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
public final class ReadNote implements PrivilegedAction<String> {
  
    static private final String CLASS_NAME = ReadNote.class.getName();
    static private final Logger LOGGER = Logger.getLogger(CLASS_NAME);
  
    private String operate (final int n) {
    
//    System.out.print("Introduce contraseña para desencriptar la nota: ");
//    final char[] password = System.console().readPassword(); 

    final String journalFile = System.getProperty("user.dir") + File.separator +
            "data" + File.separator +
            "journal.data";
    final File file = new File(journalFile);

    try (final Scanner is = new Scanner(file)) {
      
        String note = "";
        for (int notes = 0; notes < n; ++notes) {
            note = is.nextLine();
        }
      
        return note;

    } catch (final IOException |
                   NoSuchElementException ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage(), ex.getCause());
            return "";
        }

    }
  
    public String read(final int n) {
        AccessController.doPrivileged(new PrivilegedAction<String>(){
            
            @Override
            public String run() {
                try {
                    System.out.println("hola");
                    return operate( n );    
                } catch (AccessControlException ex ) {
                    LOGGER.log(Level.SEVERE, ex.getMessage(), ex.getCause());
                    return "";
                }
            }
        });
        return "";
    }

    @Override
    public String run() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
  
}