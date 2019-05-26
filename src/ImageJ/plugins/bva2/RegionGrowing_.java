import ij.IJ;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.gui.PointRoi;
import ij.gui.PolygonRoi;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;

import java.awt.*;
import java.util.Stack;
import java.util.Vector;

public class RegionGrowing_ implements PlugInFilter {

    public static int BG_VAL = 0;
    public static int FG_VAL = 255;
    public static int UNINITIALIZED = -1;
    public static int NB_ARR_RADIUS = 1;

    ImagePlus imagePlus = null;

    public int setup(String arg, ImagePlus imp) {
        if (arg.equals("about")) {
            showAbout();
            return DONE;
        }

        imagePlus = imp;
        return DOES_8G + DOES_STACKS + SUPPORTS_MASKING + ROI_REQUIRED;
    } //setup

    public static Vector<Point> GetSeedPositions(ImagePlus imagePlus) {
        PointRoi pr = (PointRoi) imagePlus.getRoi();
        int[] xCoords = pr.getXCoordinates();
        int[] yCoords = pr.getYCoordinates();
        int numOfElements = ((PolygonRoi) pr).getNCoordinates();
        Rectangle boundingRoi = pr.getBounds();

        Vector<Point> seedPositions = new Vector<Point>();

        for (int i = 0; i < numOfElements; i++) {
            seedPositions.add(new Point(xCoords[i] + boundingRoi.x, yCoords[i] + boundingRoi.y));
        }

        return seedPositions;
    }

    public void run(ImageProcessor ip) {
        byte[] pixels = (byte[]) ip.getPixels();
        int width = ip.getWidth();
        int height = ip.getHeight();
        int[][] inDataArrInt = ImageJUtility.convertFrom1DByteArr(pixels, width, height);

        Vector<Point> seedPositions = GetSeedPositions(imagePlus);
        int minVal = 0;
        int maxVal = 255;
        GenericDialog gd = new GenericDialog("User Input");
        gd.addSlider("lower thresh", minVal, maxVal, minVal);
        gd.addSlider("upper thresh", minVal, maxVal, maxVal);
        gd.showDialog();
        if (gd.wasCanceled()) {
            return;
        } //if

        int lowerThresh = (int) gd.getNextNumber();
        int upperThresh = (int) gd.getNextNumber();

        //adjacency: use N8!
        int[][] rgResultImg = PerformRegionGrowing(inDataArrInt, width, height, lowerThresh, upperThresh, seedPositions);


        ImageJUtility.showNewImage(rgResultImg, width, height, "RG result for [" + lowerThresh + ";" + upperThresh + "] and #seeds=" + seedPositions.size());

    } //run

    public static int[][] PerformRegionGrowing(int[][] inImg, int width, int height, int lowerThresh, int upperThresh, Vector<Point> seedPositions) {
        int[][] returnImg = new int[width][height];

        //first initialize all with UNINITIALIZED
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                returnImg[x][y] = UNINITIALIZED;
            }
        }

        Stack<Point> processingStack = new Stack<Point>();
        for (Point p : seedPositions) {
            if (returnImg[p.x][p.y] == UNINITIALIZED) {
                returnImg[p.x][p.y]++;
                processingStack.push(p);
            }
        }

        while (!processingStack.empty()) {
            Point currPos = processingStack.pop();
            //check interval
            int actVal = inImg[currPos.x][currPos.y];
            if (actVal >= lowerThresh && actVal <= upperThresh) {
                returnImg[currPos.x][currPos.y] = FG_VAL;

                //checkNeighbours
                for (int xOffset = -1; xOffset <= 1; xOffset++) {
                    for (int yOffset = -1; yOffset <= 1; yOffset++) {
                        int nbX = currPos.x + xOffset;
                        int nbY = currPos.y + yOffset;

                        //check range
                        if (nbX >= 0 && nbX < width && nbY >= 0 && nbY < height) {
                            if (returnImg[nbX][nbY] == UNINITIALIZED) {
                                returnImg[nbX][nbY]++;
                                processingStack.add(new Point(nbX, nbY));
                            }
                        }
                    }
                }
            } else {
                returnImg[currPos.x][currPos.y] = BG_VAL;
            }
        }

        //finally -1 (UNINITIALIZED) clean up to 0
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if(returnImg[x][y] == UNINITIALIZED)
                returnImg[x][y] = 0;
            }
        }

        return returnImg;
    }

    public static int[][] GetNeighborArrN4() {
        //TODO: implementation required
        return null;
    }

    public static int[][] GetNeighborArrN8() {
        return null;
    }

    void showAbout() {
        IJ.showMessage("About Template_...",
                "this is a RegionGrowing_ template\n");
    } //showAbout

} //class RegionGrowing_

