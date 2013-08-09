package directory;
import java.awt.Toolkit;
import java.io.*;
import javax.swing.*;
import javax.swing.tree.*;
public class DirectoryParser
{
  static String root_directory;
	static JTree tree;
	static Notifier notifier;

	public static void main(String[] args)
	{
		// Loop continuously and try for input again if it was invalid
		while (true)
		{
			// Prompt for the depth to search
			SpinnerNumberModel sModel = new SpinnerNumberModel(0, 0, 20, 1);
			JSpinner spinner = new JSpinner(sModel);
			int depth = 0;
			while (depth == 0)
			{
				depth = JOptionPane.showOptionDialog(null, spinner, "Enter maximum search depth",
					JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, null);	
				depth = (Integer) spinner.getValue();
			}
			FileNode.maxDepth = depth;
			
			// Get path input
			String input = (String) JOptionPane.showInputDialog(new JFrame(),
					"Enter a filepath for your root directory to explore:", "Directory Explorer",
					JOptionPane.QUESTION_MESSAGE, null, null, "");
			
			// "Cancel" was pressed
			if (input == null)
				System.exit(0);
			
			// Make sure the string ends with a "\"
			if (!input.endsWith("\\"))
				input += "\\";
			root_directory = input;
			
			try
			{
				create_tree();
			}
			catch (NullPointerException e) // This will be thrown for an invalid path
			{
				e.printStackTrace();
				Toolkit.getDefaultToolkit().beep();
				int option = JOptionPane.showOptionDialog(null, "That filepath does not exist on this computer." +
						"  Care to try typing in something else?", "Invalid input", JOptionPane.DEFAULT_OPTION,
						JOptionPane.INFORMATION_MESSAGE, null, new String[] {"Ok", "Exit"}, null);
				if (option == 1)
					System.exit(0);
			}
		}
	}

	public static void create_tree()
	{
		// Make the notifier
		notifier = new Notifier();
		notifier.start();
		
		// Create the tree
		FileNode root = new FileNode(root_directory + "...", root_directory);
		root.add_children();
		tree = new JTree(root);
		tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		tree.addTreeSelectionListener(new Visualizer());

		// Make the window
		JFrame window = new JFrame();
		JScrollPane pane = new JScrollPane(tree);
		window.add(pane);
		window.setVisible(true);
		window.setSize(500, 500);
		window.setTitle("Directory Explorer");
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		// Stop the notifier and loop until window is closed
		Notifier.wait = false;
		while (true);
	}
}

@SuppressWarnings("serial")
class FileNode extends DefaultMutableTreeNode
{
	static int maxDepth;
	
	///// Fields /////
	String name;
	String path;
	File file;
	long size;
	boolean isFile;
	int depth;

	public FileNode(String n, String p)
	{
		name = n;
		path = p;
		file = new File(p);
		isFile = file.isFile();
		depth = 0;
	}

	public FileNode(String p)
	{
		name = file.getName();
		path = p;
		file = new File(p);
		isFile = file.isFile();
	}

	public FileNode(File f)
	{
		name = f.getName();
		path = f.getAbsolutePath();
		file = f;
		isFile = f.isFile();
	}

	// Also computes and returns size
	public long add_children()
	{
		if (depth > maxDepth)
			return size = -1;
		if (isFile)
			size = file.length();
		else
		{
			size = 0;
			File[] next_level = file.listFiles();
			if (next_level == null)
				return 0;
			for (int i = 0; i < next_level.length; i++)
			{
				FileNode child = new FileNode(next_level[i]);
				child.depth = depth + 1;
				add(child);
				if (child.depth > maxDepth)
				{
					if (child.file.isDirectory())
					{
						child.size = -1;
						size = -2;
					}
					else
					{
						child.size = child.file.length();
						size += child.size;
					}
				}
				else
				{
					long inc_size = child.add_children();
					if (inc_size < 0 || size < 0)
						size = -2;
					else
						size += inc_size;
				}
			}
			sort_children();
		}
		return size;
	}

	public void sort_children()
	{
		// Remove all the children
		FileNode[] ar = new FileNode[getChildCount()];
		for (int i = 0; i < ar.length; i++)
			ar[i] = (FileNode) getChildAt(i);
		removeAllChildren();

		// Sort the children (bubble sort, descending order)
		for (int a = 0; a < ar.length-1; a++)
			for (int b = a+1; b < ar.length; b++)
				if (compare(ar[a], ar[b]) == -1)
				{
					FileNode x = ar[a];
					ar[a] = ar[b];
					ar[b] = x;
				}

		// Add them back in
		for (int i = 0; i < ar.length; i++)
			add(ar[i]);
	}

	public int compare(FileNode a, FileNode b)
	{
		// Folders go first
		if (a.isFile != b.isFile)
			if (a.isFile)
				return -1;
			else
				return 1;

		// Sizes next
		if (a.size < b.size)
			return -1;
		else if (a.size > b.size)
			return 1;

		return 0;
	}

	public String size_string()
	{
		String str;
		if (size == -1)
			return "More files in this folder...";
		else if (size == -2)
			return "???";
		else if (size == 0 && file.isDirectory())
			return "Empty Folder";
		else if (size < (2 << 13))
			str = size + " B";
		else if (size < (2 << 23))
			str = (size >> 10) + " KB";
		else
			str = (size >> 20) + " MB";
		return str;
	}

	public String toString()
	{
		return name + " (" + size_string() + ")";
	}
}
