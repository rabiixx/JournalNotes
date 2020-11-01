package main;

import java.util.InputMismatchException;
import java.util.Scanner;
import javax.xml.transform.TransformerException;
//
import journal.AddNote;
import journal.ReadNote;
//
public final class JournalNotes {

  /**
   * @param args the command line arguments
   */
  public static void main (final String[] args) throws TransformerException {
    
    final ReadNote readNote = new ReadNote();
    final AddNote  addNote  = new AddNote();

    final Scanner scanner = new Scanner(System.in);
    int opcion;
    do {

      System.out.println("Opciones:");
      System.out.println("  1 - Añadir nota");
      System.out.println("  2 - Leer nota");
      System.out.println("  0 - Salir");
      System.out.print("Introduce opcion: ");
      try {
        opcion = scanner.nextInt();
        scanner.nextLine();

        final String note;
        switch (opcion) {
          case 1:
            System.out.print("Introduce tu reflexión personal a guardar: ");
            note = scanner.nextLine();
            if (note.isEmpty()) {
              System.err.println("La aplicación no guarda notas vacías");
              System.out.println("Pruebe de nuevo");
              break;
            }
            
            if ( addNote.add( note ) == true ) {
              System.out.println("Nota guardada");
            } else {
              System.err.println("Problema al guardar la nota");
            }
            break;
          case 2:
            System.out.print("Introduce número de nota a recuperar: ");
            final int n = scanner.nextInt();
            scanner.nextLine();
            if (n <= 0) {
              System.err.println("Las notas se numeran desde 1");
              System.out.println("Pruebe de nuevo");
              break;
            }
            note = readNote.read(n);
            if (!note.isEmpty()) {
              System.out.println("Contenido de nota leída: " + note);
            } else {
              System.out.println("Problema al recuperar la nota");
            }
            break;
          default:
        }
      } catch (final InputMismatchException ex) {
        scanner.nextLine();
        opcion = Integer.MAX_VALUE;        
      }

    } while (opcion != 0);

  }
  
}