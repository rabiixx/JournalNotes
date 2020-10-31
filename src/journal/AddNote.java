package journal;
/**
 *
 * @author MAZ
 */
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.security.AccessControlException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;

public final class AddNote implements PrivilegedAction<Boolean>{
  
  static private final String CLASS_NAME = AddNote.class.getName();
  static private final Logger LOGGER = Logger.getLogger(CLASS_NAME);
  
  static private final int ITERATIONS = 2048;
  
  private Boolean operate (final String note) throws TransformerConfigurationException, TransformerException {

    //System.out.print("Introduce contraseña para encriptar la nota: ");
    //final char[] password = System.console().readPassword();
    System.out.println("hola");
    final String journalPath = System.getProperty("user.dir") + File.separator
            + "data" + File.separator
            + "journal.data";
    final File journalFile = new File(journalPath);
      
    try (final FileWriter os = new FileWriter(journalFile, true)) {

      os.write(note + "\n");
      return true;

    } catch (final IOException ex) {
        LOGGER.log(Level.SEVERE, ex.getMessage(), ex.getCause());
        return false;
    }

  }

  public boolean add(final String note) throws TransformerConfigurationException, TransformerException {

        return AccessController.doPrivileged( new PrivilegedAction<Boolean>() {
            @Override
            public Boolean run() {
                try {
                    Boolean res = operate(note);
                    System.out.println("res: " +res);
                    return res;
                } catch (AccessControlException | TransformerException ex) {
                    LOGGER.log(Level.SEVERE, ex.getMessage(), ex.getCause());
                    return Boolean.FALSE;
                }
            }
        });
  }

    @Override
    public Boolean run() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

  
}