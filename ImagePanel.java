import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseMotionAdapter;
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

public class ImagePanel extends JComponent implements MouseWheelListener
{
    int bogus = Integer.MIN_VALUE;
    private BufferedImage currentImage;
    private double scale = 1;
    private double zoomAmount = 1.1;
    private double placementX;
    private double placementY;
    private double drawHeight;
    private double drawWidth;
    private int mouseX = 0;
    private int mouseY = 0;
    private int oldDragX = bogus;
    private int oldDragY = bogus;

    private boolean firstPaint = true;
    private String imageFilename = "";

    private boolean canMousePress = false;
    private boolean mousePressed = false;

    private ImageTagger parent;
    public ImagePanel(ImageTagger parent)
    {
        this.parent = parent;
        addMouseWheelListener(this);
        addMouseMotionListener(mouseMotionAdapter);
        addMouseListener(mouseAdapter);
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
            //parent.debugPrint("Aspect Ratio: "+aspectRatio);
            System.out.println("Aspect Ratio: "+aspectRatio);
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

    public void fitImage()
    {
        //find out how much larger each dimension of the image is than the window;
        double overWidth = (double)currentImage.getWidth() / (double)getWidth();
        double overHeight = (double)currentImage.getHeight() / (double)getHeight();
        if(overWidth >= overHeight)
        {
            //if the width is bigger, draw the sides of the image up to the window edges,
            // effectively scaling it down by overWidth,
            //and scaling the verticals down by the same amount by
            // setting the height to the normal height / overWidth.
            drawWidth = getWidth();
            drawHeight = (double)currentImage.getHeight()/overWidth;
            placementX = 0;
            placementY = (getHeight()/2) - drawHeight/2;//centre the image's Y.

            //double imgHeight = (double)currentImage.getHeight()/overWidth;
            //placementY = (getHeight()/2) - imgHeight/2;//centre the image's Y.

            //g.drawImage(currentImage, 0, (int)placementY, getWidth(), (int)imgHeight, this);
            scale = 1.0 / overWidth;
        }
        else if (overWidth < overHeight)
        {
            //same here, but vice versa.
            drawWidth = (double)currentImage.getWidth()/overHeight;
            drawHeight = getHeight();
            placementX = (getWidth()/2) - drawWidth/2;//centre the image's X.
            placementY = 0;
            //g.drawImage(currentImage, (int)placementX, 0, (int)imgWidth, getHeight(), this);
            scale = 1.0 / overHeight;
        }
    }

    public void normalZoom()
    {
        //System.out.println("normal zoom!");
        scale = 1;
        //drawWidth = (double)currentImage.getWidth();
        //drawHeight = (double)currentImage.getHeight();

        //g.drawImage(currentImage, (int)placementX, (int)placementY,(int)placementWidth, (int)placementHeight, this);

        //parent.stdPrint("current image width:"+placementWidth);
        //parent.stdPrint("current image height:"+placementHeight);
    }

    public void paint(Graphics g)
    {
        if(currentImage != null)//only do stuff if we have an image to draw -- prevents NullPointerExceptions
        {
            Graphics2D g2 = (Graphics2D)g;
//find out how much larger each dimension of the image is than the window;

//before drawing the new image, clear the old one.
            g.clearRect(0,0,getWidth(),getHeight());
            System.out.println("firstPaint:"+firstPaint);
            if(firstPaint)//first time we draw a new image, make it fit the window
            {
//find out how much larger each dimension of the image is than the window;
                firstPaint = false;

                double overWidth = (double)currentImage.getWidth() / (double)getWidth();
                double overHeight = (double)currentImage.getHeight() / (double)getHeight();
                if(overWidth > 1.0
                        || overHeight > 1.0)
                {
                    fitImage();
                }
                else
                {
                    normalZoom();
                }
                System.out.println("shaboieeee");
            }
            else
            {//no longer first paint -- do zooming to mouse
                drawWidth = (double)currentImage.getWidth() * scale;
                drawHeight = (double)currentImage.getHeight() * scale;

//g.drawImage(currentImage, (int)placementX, (int)placementY,
//(int)placementWidth, (int)placementHeight, this);

//parent.stdPrint("current image width:"+placementWidth);
//parent.stdPrint("current image height:"+placementHeight);
                parent.stdPrint("zoom:"+(scale*100));
            }
//draw the image with the inferred parameters
            g.drawImage(currentImage, (int)placementX, (int)placementY, (int)drawWidth, (int)drawHeight, this);

            parent.setTitle(imageFilename+" -- "+((int)(scale*100))+"%");
        }
    }

    private MouseAdapter mouseAdapter = new MouseAdapter()
    {
        @Override
        public void mouseEntered(MouseEvent me)
        {
//System.out.println("enter");
            canMousePress = true;
        }

        @Override
        public void mouseExited(MouseEvent me)
        {
//System.out.println("exit");
            canMousePress = false;
        }

        @Override
        public void mousePressed(MouseEvent me)
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

        @Override
        public void mouseReleased(MouseEvent me)
        {
//System.out.println("release");
            if(canMousePress)
                mousePressed = false;
        }
    };
        
    private MouseMotionAdapter mouseMotionAdapter = new MouseMotionAdapter()
    {
        @Override
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
    };

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
