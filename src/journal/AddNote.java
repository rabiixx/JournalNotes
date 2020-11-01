package journal;
/**
 *
 * @author MAZ
 */
import java.io.Console;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.security.AccessControlException;
import java.security.AccessController;
import java.security.GeneralSecurityException;
import java.security.PrivilegedAction;
import java.util.Arrays;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.Cipher;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;

public final class AddNote {
  
  static private final String CLASS_NAME = AddNote.class.getName();
  static private final Logger LOGGER = Logger.getLogger(CLASS_NAME);
  
  static private final int ITERATIONS = 2048;
  
  private Boolean operate (final String note) throws TransformerConfigurationException, TransformerException {

    Console console = null;
    try {
        console = System.console();
        if ( console != null ) {
            System.out.print("Introduce contraseña para encriptar la nota: ");
            final char[] passwd = System.console().readPassword();
            System.out.println("Password: " + new String(passwd));
            
            try {   
                AESCipherGenerator acg = new AESCipherGenerator();
                
                Cipher cipher = acg.getEncrypter(passwd, ITERATIONS);
                
                byte[] encryptedMsg = cipher.doFinal( note.getBytes() );
                //System.out.println("base64 encode " + Arrays.toString(encryptedMsg));
                String encodedMsg = Base64.getEncoder().encodeToString( encryptedMsg );
                
                final byte[] params = cipher.getParameters().getEncoded();
                final String base64Params = Base64.getEncoder().encodeToString(params);
                
                final String journalPath = System.getProperty("user.dir") + File.separator
                        + "data" + File.separator
                        + "journal.data";
                
                
                final File journalFile = new File(journalPath);
                
                

                try (final FileWriter os = new FileWriter(journalFile, true)) {

                    /** 
                      * format: encodedMsg:encodedParams
                      * ( ":" is not a valid base64 character, so is used as delimiter )
                      */
                    os.write(encodedMsg + ":" + base64Params + "\n");
                    return Boolean.TRUE;

                } catch (final IOException ex) {
                    LOGGER.log(Level.SEVERE, ex.getMessage(), ex.getCause());
                    return Boolean.FALSE;
                }
            
            } catch (GeneralSecurityException ex) {
                LOGGER.log(Level.SEVERE, ex.getMessage(), ex.getCause());
                return Boolean.FALSE;
            }
        }
    } catch (Exception ex) {
        LOGGER.log(Level.SEVERE, ex.getMessage(), ex.getCause());
        return Boolean.FALSE;
    }
    
    return Boolean.TRUE;
}

  public Boolean add(final String note) throws TransformerConfigurationException, TransformerException {

        return AccessController.doPrivileged( new PrivilegedAction<Boolean>() {
            @Override
            public Boolean run() {
                try {
                    return operate(note);
                } catch (AccessControlException | TransformerException ex) {
                    LOGGER.log(Level.SEVERE, ex.getMessage(), ex.getCause());
                    return Boolean.FALSE;
                }
            }
        });
    }
}