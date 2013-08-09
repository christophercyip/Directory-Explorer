package directory;
import java.awt.*;
import javax.swing.*;
import javax.swing.event.*;
public class Visualizer implements TreeSelectionListener
{
  static JFrame window;

	public void valueChanged(TreeSelectionEvent e)
	{
		// Update the selected node
		FileNode node = (FileNode) DirectoryParser.tree.getLastSelectedPathComponent();
		if (node == null || node.isLeaf())
			return;

		// Delete the old window
		if (window != null)
			window.dispose();

		// Create a new layout and window for everything to go into
		window = new JFrame();
		GridLayout layout = new GridLayout(0,2);
		window.setLayout(layout);

		// Separate folders from files
		if (!node.getChildAt(0).isLeaf())
		{
			window.add(new JLabel("Folders:"));
			window.add(new JLabel(" "));
		}

		// Find the largest known file
		long largest = 1;
		int count = node.getChildCount();
		for (int index = 0; index < count; index++)
		{
			FileNode curr = ((FileNode) node.getChildAt(index));
			if (curr.size > largest)
				largest = curr.size;
		}
		
		// Add the children and make progress bars indicating space taken
		boolean flag = false;
		for (int i = 0; i < Math.min(node.getChildCount(), 30); i++)
		{			
			FileNode child = (FileNode) node.getChildAt(i);

			// Folders are all shown
			if (child.isLeaf() && child.file.isFile() && !flag)
			{
				window.add(new JLabel("Files:"));
				window.add(new JLabel(" "));
				flag = true;
			}

			window.add(new JLabel("     " + child.name));

			// Visualization of file size
			JProgressBar bar = new JProgressBar();
			double fraction = 1000f * child.size / largest;
			bar.setMaximum(1000);
			bar.setValue((int) fraction);
			bar.setString(child.size_string());
			bar.setStringPainted(true);
			window.add(bar);
		}
		
		if (node.getChildCount() > 30)
		{
			window.add(new JLabel("There are actually another " + (node.getChildCount() - 30) + " files in here,"));
			window.add(new JLabel("          Total size of other files:"));
			window.add(new JLabel("but they wouldn't fit in the window properly..."));
			long total = 0;
			
			// Total up other file sizes
			for (int i = 30; i < node.getChildCount(); i++)
			{
				long inc = ((FileNode) node.getChildAt(i)).size;
				if (inc >= 0)
					total += inc;
				else
				{
					total = -2;
					break;
				}
			}
			
			// Make a final bar
			JProgressBar bar = new JProgressBar();
			double fraction = 1000f * total / largest;
			bar.setMaximum(1000);
			bar.setValue((int) fraction);
			bar.setString(size_string(total));
			bar.setStringPainted(true);
			
			// Bar is "over-full"
			if (total > largest)
				bar.setForeground(Color.red);
			
			window.add(bar);
		}

		// Show the window
		window.setVisible(true);
		window.pack();
		window.setLocation(510, 0);
	}
	
	public static String size_string(long size)
	{
		String str;
		
		if (size == -1)
			return "Maximum depth.";
		else if (size == -2)
			return "???";
		
		if (size < (2 << 13))
			str = size + " B";
		else if (size < (2 << 23))
			str = (size >> 10) + " KB";
		else
			str = (size >> 20) + " MB";
		return str;
	}
}
