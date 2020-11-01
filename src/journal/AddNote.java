package journal;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.security.AccessControlException;
import java.security.AccessController;
import java.security.GeneralSecurityException;
import java.security.PrivilegedAction;
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

        try {

            System.out.print("Introduce contraseña para encriptar la nota: ");
            final char[] passwd = System.console().readPassword();

            AESCipherGenerator acg = new AESCipherGenerator();

            /* Obtenemos el cipher a utilizar para encriptar el mensaje*/
            Cipher cipher = acg.getEncrypter(passwd, ITERATIONS);

            /* Enctiptamos el mensaje y nos lo devulve encriptado */
            byte[] encryptedMsg = cipher.doFinal( note.getBytes() );

            /* Codificamos el mensaje encriptado en base64 */
            String encodedMsg = Base64.getEncoder().encodeToString( encryptedMsg );

            /** 
              * Obtenemos los parámetros (pizca de sal, iteraciones y vector de 
              * inicialización utilizados por el cipher para poder desencriptar 
              * el mensaje posteriormente.
              */
            final byte[] params = cipher.getParameters().getEncoded();

            /* Al igual que el mensaje, codificamos los paramentros en base64 */
            final String base64Params = Base64.getEncoder().encodeToString(params);


            final String journalPath = System.getProperty("user.dir") + File.separator
                + "data" + File.separator
                + "journal.data";

            final File journalFile = new File(journalPath);

            /**
             * Escribimos el mensaje encriptado y los parámetros utilizados para
             * su encriptacion en el fichero journal.data con el siguiente formato:
             * formato: encodedMsg:encodedParams
             */
            try (FileWriter os = new FileWriter(journalFile, true)) {
                os.write(encodedMsg + ":" + base64Params + "\n");
                return Boolean.TRUE;
            } catch (final IOException ex) {
                LOGGER.log(Level.SEVERE, ex.getMessage(), ex.getCause());
                return false;
            }
            
        } catch ( IOException | GeneralSecurityException ex ) {
            LOGGER.log(Level.SEVERE, ex.getMessage(), ex.getCause());
            return Boolean.FALSE;
        }
        
    }

    public Boolean add(final String note) throws TransformerConfigurationException, TransformerException {

        return AccessController.doPrivileged((PrivilegedAction<Boolean>) () -> {
            try {
                return operate( note );
            } catch (AccessControlException | TransformerException ex) {
                LOGGER.log(Level.SEVERE, ex.getMessage(), ex.getCause());
                return Boolean.FALSE;
            }
        });
    }
}