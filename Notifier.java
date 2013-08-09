package directory;
import java.awt.*;
import javax.swing.*;
public class Notifier extends Thread
{
  static boolean wait;
	public void run()
	{
		wait = true;
		
		try
		{
			int option = 0;
			while (option == 0)
			{
				Thread.sleep(60000);
				while (!wait);
				Toolkit.getDefaultToolkit().beep();
				option = JOptionPane.showOptionDialog(null, "The program has been scanning through your file" +
						" directory for a while now;\n there might be a ton of things in the place you told it" +
						" to search.  Would you like\n to give it another minute, or just exit the program?\n" +
						"If you still want to look in this folder, try a smaller search depth.",
						"Lots of files, huh?", JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE,
						null, new String[] {"Wait", "Exit"}, null);
			}
			System.exit(0);
		}
		catch (InterruptedException e)
		{
			Toolkit.getDefaultToolkit().beep();
			JOptionPane.showOptionDialog(null, "Notifier interrupted!", "I AM ERROR", JOptionPane.DEFAULT_OPTION,
					JOptionPane.WARNING_MESSAGE, null, new String[] {"Exit Program"}, null);
			System.exit(-1);
		}
	}
}
