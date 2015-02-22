import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.event.*;
import java.awt.BorderLayout;
import java.io.File;
import java.io.IOException;
import java.io.FilenameFilter;
import java.io.PrintStream;
import java.awt.image.BufferedImage;

import java.lang.IllegalArgumentException;
import java.lang.Runtime;
import javax.imageio.stream.FileImageInputStream;

public class ImageTagger extends JFrame implements ActionListener
{
	private static Runtime rt = Runtime.getRuntime();
	public ImagePanel imagePanel = new ImagePanel(this);
	private int currentImageIndex;
	private JPanel bottomButtonPanel = new JPanel();
	private static FileNameExtensionFilter imagesOnly =
		new FileNameExtensionFilter("Images", "jpg", "jpeg", "png", "gif");
	private File[] filesToRead;
	private JButton prevButton;
	private JButton nextButton;
	private BufferedImage currentImage;
	private File debugFile = new File("debug.log");
	private PrintStream stdOut = System.out;
	private PrintStream dbgOut;
	private boolean debugMessagesEnabled = false;
	private boolean standardMessagesEnabled = true;
	
	public ImageTagger()
	{
		try
		{UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());}
		catch(Exception bob){System.err.print("Failed to set look and feel.");}
		
		if(debugMessagesEnabled)
		{
			try
			{
				//initialise debug outfile
				if(!debugFile.exists())
				{debugFile.createNewFile();}
				debugFile.deleteOnExit();
				try{dbgOut = new PrintStream(debugFile);}
				catch(Exception e){System.err.println("faillllsss");}
			}
			catch(IOException e)
			{System.err.println("failed to create debug log!");}
		}
		//configure main window
		setLayout(new BorderLayout(5, 5) );
		setTitle("Image Validator");
		setSize(600, 600);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		//add the imagepanel
		add(imagePanel);

		//configure the buttonPanel before displaying window. This allows the panel to display properly on startup.
		bottomButtonPanel.setLayout(new BorderLayout(5, 5) );
		ImageIcon prevIcon = new ImageIcon("images/go-previous.png");
		ImageIcon nextIcon = new ImageIcon("images/go-next.png");
		ImageIcon prevIconDisabled = new ImageIcon("images/go-previous-disabled.png");
		ImageIcon nextIconDisabled = new ImageIcon("images/go-next-disabled.png");
		prevButton = new JButton(prevIcon);
		nextButton = new JButton(nextIcon);
		nextButton.setDisabledIcon(nextIconDisabled);
		prevButton.setDisabledIcon(prevIconDisabled);
		bottomButtonPanel.add(prevButton, BorderLayout.WEST);
		bottomButtonPanel.add(nextButton, BorderLayout.EAST);
		//bottomButtonPanel.repaint(bottomButtonPanel.getBounds());
		
		//add the buttonPanel to the main window, and make the main window visible.
		add(bottomButtonPanel, BorderLayout.SOUTH);
		setVisible(true);
		
		//set this class as the actionListener for the next and previous buttons.
		nextButton.addActionListener(this);
		prevButton.addActionListener(this);
		
		//debug messages from validator code.
		debugPrint("Max Memory: " + (rt.maxMemory() / 1048576) + "MB" );
		debugPrint("Current Total Memory: " + (rt.totalMemory() / 1048576) + "MB");
		String cwd = System.getProperty("user.dir");
		debugPrint("the imagePanel is double buffered: "+imagePanel.isDoubleBuffered());
		
		//keep reshowing the fileChooser dialog until the user makes a choice which is valid (ie returns true).	
		while( (validateSelection(chooseFile(cwd))) == false)
		{continue;}
	}
	
	public void debugPrint(String msg)
	{
		if(debugMessagesEnabled)
		{
			dbgOut.println(msg);
		}
	}
	
	public void stdPrint(String msg)
	{
		if(standardMessagesEnabled)
		{
			stdOut.println(msg);
		}
	}
	
	private File chooseFile(String cwd)
	{
		JFileChooser fc = new JFileChooser(new File(cwd) );
		
		fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		fc.setFileFilter(imagesOnly);
		fc.setMultiSelectionEnabled(false);
		int exitState = fc.showOpenDialog(null);
		if(exitState == JFileChooser.APPROVE_OPTION)
		{
			return fc.getSelectedFile();
			//if this method doesn't return here, it hasn't exited corrrectly, so elsewise return null.
		}
		return null;
	}
	
	private boolean validateSelection(File loadedFile)
	{
		//use a different file filter, which oddly has to be separate 
		//from the one for the fileChooser
		FilenameFilter imagesFilteur = new ExtensionFilter("jpg", "jpeg", "png", "gif");
		stdPrint("name: "+loadedFile.getName());
		if(loadedFile.isDirectory())
		{
			stdPrint("Directory detected.");
			//check to see if the user has selected the .. or . virtual
			//directories, which cause erratic behaviour.
			while(!loadedFile.exists()
			|| loadedFile.getName().equals("..")
			|| loadedFile.getName().equals("."))
			{
				stdPrint("Specified directory does not exist. Getting parent...");
				loadedFile = loadedFile.getParentFile();
			}
			//create the File[] which contains the images to navigate between.
			filesToRead = loadedFile.listFiles(imagesFilteur);
			if(filesToRead.length < 1)
			{
				//check to see if the selected directory contains any images.
				//still need to add a similar check to test if 
				//the selected file is actually an image, if it's not a directory.
				stdPrint("Directory contains no images! Please rechoose an image, or a directory containing images...");
				return false;
			}
			//select and load the first image in the directory;
			//this also means the previous button should be disabled for now.
			currentImageIndex = 0;
			prevButton.setEnabled(false);
			imagePanel.loadImage(filesToRead[0]);
		}
		else
		{
			//the selected File is not a directory;
			//check to see if it ends with a valid image extension
			if(imagesFilteur.accept(loadedFile.getParentFile(), loadedFile.getName()))
			{
				//check to make sure the file is a decent length
				if(loadedFile.length() > 100)
				{
					//list the Files (with image filter) in the parent directory of the selected image
					filesToRead = loadedFile.getParentFile().listFiles(imagesFilteur);
					//the current index is the loadedFile's position in the File[] of its parent listing.
					currentImageIndex = java.util.Arrays.asList(filesToRead).indexOf(loadedFile);
					imagePanel.loadImage(filesToRead[currentImageIndex]);
				}
				else
				{
					stdPrint("Selected file is not a valid image! Please rechoose a valid image.");
					return false;
				}
			}
			else
			{//file not accepted by the images filter; probably doesn't 
				//have a file extension for an image
				stdPrint("Selected file is not an image! Please rechoose a valid image.");
				return false;
			}
		}
		return true;
	}
	
	public void actionPerformed(ActionEvent e)
	{
		assert e.getSource() instanceof JButton;
		JButton jb = (JButton)e.getSource();
		if(jb == nextButton)
		{
			currentImageIndex++;
			if(currentImageIndex >= filesToRead.length-1)
			{
				currentImageIndex = filesToRead.length-1;
				nextButton.setEnabled(false);
			}
			else
			{prevButton.setEnabled(true);}
		}
		else if(jb == prevButton)
		{
			currentImageIndex--;
			if(currentImageIndex <= 0)
			{
				currentImageIndex = 0;
				prevButton.setEnabled(false);
			}
			else
			{nextButton.setEnabled(true);}
		}
		else
		{System.err.println("failed to click a button: "+jb);}
		
		imagePanel.loadImage(filesToRead[currentImageIndex]);
		long usedMem = rt.totalMemory() - rt.freeMemory();
		debugPrint("Used memory: "+ (usedMem/1024)+"KB");	
	}
	
	public static void main(String[] args)
	{
		//System.setProperty("java.awt.headless", "false");
		//debugPrint("isHeadless: "+GraphicsEnvironment.isHeadless() );
		ImageTagger imgTag = new ImageTagger();
	}
}
