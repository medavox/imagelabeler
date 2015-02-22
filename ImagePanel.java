import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.event.MouseWheelListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseEvent;
import javax.swing.JComponent;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.PrintStream;
import java.io.IOException;
import java.awt.Graphics2D;
//import javax.swing.JFrame;
//import java.awt.geom.AffineTransform;
//import java.awt.image.AffineTransformOp;

public class ImagePanel extends JComponent implements MouseListener, MouseWheelListener, MouseMotionListener
{
	private BufferedImage currentImage;
	private double scale = 1;
	private double zoomAmount = 1.1;
	private int imageWidth = 0;
	private int imageHeight = 0;
	private double placementX;
	private double placementY;
	private int mouseX = 0;
	private int mouseY = 0;
	
	private int oldDragX = -99999;
	private int oldDragY = -99999;
	
	private boolean firstPaint = true;
	private String imageFilename = "";
	
	private boolean canMousePress = false;
	private boolean mousePressed = false;
	
	private ImageTagger parent;
	public ImagePanel(ImageTagger parent)
	{
		this.parent = parent;
		addMouseWheelListener(this);
		addMouseMotionListener(this);
		addMouseListener(this);
	}
	
	public void loadImage(File imageToLoad)
	{
		try
		{
			currentImage = ImageIO.read(imageToLoad);
			parent.setTitle(imageToLoad.getName());//change title to filename
			imageFilename = imageToLoad.getName();
			//debugging info
			parent.debugPrint("Image Width: "+currentImage.getWidth());
			parent.debugPrint("Image Height: "+currentImage.getHeight());
			double aspectRatio = (double)currentImage.getWidth() / (double)currentImage.getHeight();
			parent.debugPrint("Aspect Ratio: "+aspectRatio);
			firstPaint = true;
			repaint();
		}
		//Very old catch blocks from image validator code.
		//Could still come in handy...
		catch(IOException e)
		{System.err.println("IO Exception in \n" + imageToLoad);}
		catch(IllegalArgumentException iaError)
		{System.err.println("\n Illegal Argument Exception: " +
			iaError.getMessage()+"\nin " + imageToLoad);}
		catch(OutOfMemoryError memError)
		{System.err.println("OutOfMemoryError: "+memError.getMessage()+
			" on file "+imageToLoad+"; continuing ONWARDS!");}
	}
	
	public void paint(Graphics g)
	{
		if(currentImage != null)//only do stuff if we have an image to draw -- prevents NullPointerExceptions
		{
			Graphics2D g2 = (Graphics2D)g;
			//find out how much larger each dimension of the image is than the window;
			double overWidth = (double)currentImage.getWidth() / (double)getWidth();
			double overHeight = (double)currentImage.getHeight() / (double)getHeight();
			
			//before drawing the new image, clear the old one.
			g.clearRect(0,0,getWidth(),getHeight());
				/*AffineTransform at = AffineTransform.getScaleInstance(scaleAmount, scaleAmount);
				AffineTransformOp aop = new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR );
				g2.drawImage(currentImage, aop, 0, 0);
				g.drawImage(bi,
				0, 0, w, h,     
                0, 0, w/2, h/2,
                null)*/
				/*g2.drawImage(currentImage, 
				AffineTransform.getScaleInstance(scaleAmount, scaleAmount),
				 this);*/
			if(firstPaint)//first time we draw a new image, make it fit the window
			{
				firstPaint = false;
				if(overWidth >= overHeight)
				{
					//if the width is bigger, draw the sides of the image up to the window edges,
					// effectively scaling it down by overWidth,
					//and scaling the verticals down by the same amount by
					// setting the height to the normal height / overWidth.
					double imgHeight = (double)currentImage.getHeight()/overWidth;
					placementY = (getHeight()/2) - imgHeight/2;//centre the image's Y.
					g.drawImage(currentImage, 0, (int)placementY, getWidth(), (int)imgHeight, this);
					scale = 1.0 / overWidth;
				}
				else if (overWidth < overHeight)
				{
					//same here, but vice versa.
					double imgWidth = (double)currentImage.getWidth()/overHeight;
					placementX = (getWidth()/2) - imgWidth/2;//centre the image's X.
					g.drawImage(currentImage, (int)placementX, 0, (int)imgWidth, getHeight(), this);
					scale = 1.0 / overHeight;
				}
				else
				{//neither dimension of the image is bigger than the viewing area;
					//draw image to normal scale
					placementX = 
						(getWidth()/2) - currentImage.getWidth()/2;//centre the image's X.
					placementY = 
						(getHeight()/2) - currentImage.getHeight()/2;//centre the image's Y.
					g.drawImage(currentImage, (int)placementX, (int)placementY,
					 currentImage.getWidth(), currentImage.getHeight(), this);
				}
				System.out.println("placementX,Y:"+placementX+","+placementY);
			}
			else
			{//no longer first paint -- do zooming to mouse
				double placementWidth = (double)currentImage.getWidth() * scale;
				double placementHeight = (double)currentImage.getHeight() * scale;

				g.drawImage(currentImage, (int)placementX, (int)placementY,
				(int)placementWidth, (int)placementHeight, this);

				//parent.stdPrint("current image width:"+placementWidth);
				//parent.stdPrint("current image height:"+placementHeight);
			}
			
			parent.setTitle(imageFilename+" -- "+((int)(scale*100))+"%");
			/*//attempt at one-case-fits-all generalised code -- 
			 * //sadly scales the smaller dimension up to the viewing area atm
			 * //-- needs to preserve aspect ratio
				double placementX = Math.max((getWidth()/2) - currentImage.getWidth()/2, 0);//centre the image's X.
				double placementY = Math.max((getHeight()/2) - currentImage.getHeight()/2, 0);//centre the image's Y.
				int placementWidth = Math.min(currentImage.getWidth(),getWidth());
				int placementHeight = Math.min(currentImage.getHeight(),getHeight());
				g.drawImage(currentImage,
						(int)placementX,
						(int)placementY,
						placementWidth,
						placementHeight,
						this);*/
		}
	}
	
	public void mouseMoved(MouseEvent me)
	{}//empty
	
	public void	mouseClicked(MouseEvent me)
	{}
	public void	mouseEntered(MouseEvent me)
	{
		//System.out.println("enter");
		canMousePress = true;
	}
	
	public void	mouseExited(MouseEvent me)
	{
		//System.out.println("exit");
		canMousePress = false;
	}
	
	public void	mousePressed(MouseEvent me)
	{
		//System.out.println("press");
		System.out.println("button pressed:"+me.getButton());
		if(canMousePress)
		{
			mousePressed = true;
			oldDragX = me.getX();
			oldDragY = me.getY();
		}
	}
	public void	mouseReleased(MouseEvent me)
	{
		//System.out.println("release");
		if(canMousePress)
			mousePressed = false;
	}
	
	public void mouseDragged(MouseEvent me)
	{
		/*if(oldDragX == -99999
		|| oldDragY == -99999)
		{
			oldDragX = me.getX();
			oldDragY = me.getY();
			return;
		}*/
		//System.out.println("dragging");
		if(mousePressed)
		{
			if(firstPaint)
			{
				oldDragX = me.getX();
				oldDragY = me.getY();
			}
			else
			{
				/*System.out.println("oldDragX,Y:"+oldDragX+","+oldDragY);
				System.out.println("mouseX,Y:"+me.getX()+","+me.getY());
				System.out.println("placementX,Y:"+placementX+","+placementY);*/
				
				int dragDiffX = me.getX() - oldDragX;
				int dragDiffY = me.getY() - oldDragY;
				
				//System.out.println("dragDiffX,Y:"+dragDiffX+","+dragDiffY);
				
				placementX += dragDiffX;
				placementY += dragDiffY;
				
				oldDragX = me.getX();
				oldDragY = me.getY();
				
				repaint();
			}
		}
	}
	
	public void mouseWheelMoved(MouseWheelEvent mwe)
	{
		//parent.stdPrint("MouseWheelEvent coords: "+mwe.getX()+","+mwe.getY());
		mouseX = mwe.getX();
		mouseY = mwe.getY();

		//parent.stdPrint("placementX,Y:"+placementX+","+placementY);
		
		double pixelsLeft = mouseX - placementX;
		double pixelsAbove = mouseY - placementY;
		
		//parent.stdPrint("pixelsLeft: "+pixelsLeft);
		//parent.stdPrint("pixelsAbove: "+pixelsAbove);
		
		double oldScale = scale;
		
		double newPixelsAbove;
		double newPixelsLeft;
		//update the scale;
		if(mwe.getWheelRotation() < 0)
		{//zooming in
			parent.stdPrint("zooming in");
			scale *= zoomAmount;
			repaint();
			 newPixelsAbove = pixelsAbove * zoomAmount;
			newPixelsLeft = pixelsLeft * zoomAmount;
		}
		else
		{//zooming out
			parent.stdPrint("zooming out");
			scale /= zoomAmount;
			repaint();
			newPixelsAbove = pixelsAbove / zoomAmount;
			newPixelsLeft = pixelsLeft / zoomAmount;
		}
		//parent.stdPrint("scale/oldScale="+(scale/oldScale));
		//parent.stdPrint("oldScale/scale="+(oldScale/scale));
		
		//WELL BLOODY FRIGGING DONE TO ME FOR DERIVING A ZOOM ALGORITHM FROM SCRATCH ON MY OWN! :D
		//todo: tidy up bad int and double conversions, avoid double arithmetic where possible
			//-- to stop the zooming messing up towards the extremes
		parent.stdPrint("scale change: "+oldScale+" to "+scale);
		
		//parent.stdPrint("new pixelsLeft: "+newPixelsLeft);
		//parent.stdPrint("new pixelsAbove: "+newPixelsAbove);
		
		placementX += (pixelsLeft - newPixelsLeft);//YUSSS I HAVE FUCKING DONE IT AFTER 4 MONTHS
		placementY += (pixelsAbove - newPixelsAbove);
		
		parent.stdPrint("new placementX,Y:"+placementX+","+placementY);		
	}
}
