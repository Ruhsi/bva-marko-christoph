import ij.IJ;
import ij.ImagePlus;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Vector;


public class HoughTransformIrisDetection_ implements PlugInFilter {


    public int setup(String arg, ImagePlus imp) {
        if (arg.equals("about")) {
            showAbout();
            return DONE;
        }
        return DOES_RGB + DOES_STACKS + SUPPORTS_MASKING + ROI_REQUIRED;
    } //setup


    public void run(ImageProcessor ip) {
        BufferedImage buffImage = ip.getBufferedImage();

        int width = ip.getWidth();
        int height = ip.getHeight();

        //convert to grayscale
        //use weighted conversion instead of mean
        //0.299 * red + 0.587 * green + 0.114 * blue
        double[][] grayscaleImg = ImageJUtility.getGrayscaleImgFromRGB(ip, ImageJUtility.CONVERSION_MODE_RGB_GRAYSCALE_WEIGHTED);

        //apply convolution filter for smoothing
        ImageJUtility.showNewImage(grayscaleImg, width, height, "gray");
        int[][] grayscaleImgInt = ImageJUtility.anisotropicDiffusion(grayscaleImg, width, height);
        grayscaleImg = ImageJUtility.convertToDoubleArr2D(grayscaleImgInt, width, height);
        ImageJUtility.showNewImage(grayscaleImg, width, height, "gray after ad");

        //perform edge detection
        double[][] grayscaleImgSobelH = ImageJUtility.sobel(ip, grayscaleImg, ImageJUtility.SOBELH);
        double[][] grayscaleImgSobelV = ImageJUtility.sobel(ip, grayscaleImg, ImageJUtility.SOBELV);

        // merge the sobelV and sobelH images
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                grayscaleImg[i][j] = Math.round(Math.sqrt(
                        Math.pow(grayscaleImgSobelV[i][j], 2) +
                                Math.pow(grayscaleImgSobelH[i][j], 2)
                ));
            }
        }

        // show grayscale image with anisotropic diffusion after sobel
        ImageJUtility.showNewImage(grayscaleImg, width, height, "with AD");

        //now restrict to sub-image
        Rectangle roiSelection = ip.getRoi();
        int roiWidth = roiSelection.width;
        int roiHeight = roiSelection.height;

        // crop the image to roi and show the cropped image
        double[][] croppedImg = ImageJUtility.cropImage(grayscaleImg, roiWidth, roiHeight, roiSelection);
        ImageJUtility.showNewImage(croppedImg, roiWidth, roiHeight, "cropped");

        //now generate the hough space
        HoughSpace houghSpace = genHoughSpace(croppedImg, roiWidth, roiHeight);

        //now chart the result ==> pixels in red
        double a = houghSpace.bestX - houghSpace.bestR * Math.cos(0 * Math.PI / 180);
        double b = houghSpace.bestY - houghSpace.bestR * Math.sin(90 * Math.PI / 180);
        ip.setColor(Color.RED);
        ip.drawOval(roiSelection.x + (int) a, roiSelection.y + (int) b, 2 * houghSpace.bestR, 2 * houghSpace.bestR);

        //finally plot 2D image for best radius and MIP image in direction of the radius
        plotBestRadiusSpace(houghSpace);
        plotRadiusMIPSpace(houghSpace);
    } //run

    void showAbout() {
        IJ.showMessage("About Template_...",
                "this is a PluginFilter template\n");
    } //showAbout

    public void plotBestRadiusSpace(HoughSpace hs) {
        double[][] bestRadii = new double[hs.width][hs.height];

        for (int x = 0; x < hs.width; x++) {
            for (int y = 0; y < hs.height; y++) {
                bestRadii[x][y] = scaleValueBetween(hs.houghSpace[x][y][hs.bestR],
                        0, 255, 0, (int) hs.houghSpace[hs.bestX][hs.bestY][hs.bestR]);
            }
        }

        ImageJUtility.showNewImage(bestRadii, hs.width, hs.height, "BestRadiusSpace");
    }

    public void plotRadiusMIPSpace(HoughSpace hs) {
        double[][] bestRadii = new double[hs.width][hs.height];

        for (int x = 0; x < hs.width; x++) {
            for (int y = 0; y < hs.height; y++) {
                double bestR = 0;
                for (int r = 0; r < hs.houghSpace[x][y].length; r++) {
                    if (hs.houghSpace[x][y][r] > bestR) {
                        bestR = hs.houghSpace[x][y][r];
                    }
                }
                int best = (int) hs.houghSpace[hs.bestX][hs.bestY][hs.bestR];
                bestRadii[x][y] = scaleValueBetween(bestR, 0, 255, 0, best);
            }
        }
        ImageJUtility.showNewImage(bestRadii, hs.width, hs.height, "RadiusMIPSpace");
    }


    public HoughSpace genHoughSpace(double[][] edgeImage, int width, int height) {
        // first calculate the parameter range
        // then evaluate fitness for each parameter permutation
        int radius;
        if (height < width) {
            radius = height / 2;
        } else {
            radius = width / 2;
        } // sets a 3D space array of ints to hold 'hits' in x, y, and r planes
        int minRadius = 10;
        HoughSpace hs = new HoughSpace(width, height, radius, minRadius);
        for (int rad = minRadius; rad < radius; rad++) {
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    for (int t = 0; t <= 360; t++) {
                        Integer a = (int) Math.floor(x - rad * Math.cos(t * Math.PI / 180));
                        Integer b = (int) Math.floor(y - rad * Math.sin(t * Math.PI / 180));
                        if (edgeImage[x][y] > 25) {
                            if (!((0 > a || a > width - 1) || (0 > b || b > height - 1))) {
                                if (!(a.equals(x) && b.equals(y))) {
                                    hs.houghSpace[a][b][rad] += 1;
                                }
                            }
                        }
                    }
                }
            }
        } // then evaluate fitness for each parameter permutation
        double max = 0;
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                for (int r = 5; r < radius; r++) {
                    if (hs.houghSpace[x][y][r] > max) {
                        max = hs.houghSpace[x][y][r];
                        hs.bestX = x;
                        hs.bestY = y;
                        hs.bestR = r;
                    }
                }
            }
        }

        return hs;
    }

    public class HoughSpace {
        double[][][] houghSpace;
        int width;
        int height;

        int bestX;
        int bestY;
        int bestR;

        int minRadius;
        int radiusRange;

        double bestWeight = 0.0;

        public HoughSpace(int width, int height, int radiusRange, int minRadius) {
            this.width = width;
            this.height = height;
            this.bestR = -1;
            this.bestX = -1;
            this.bestY = -1;
            this.bestWeight = 0.0;
            this.minRadius = minRadius;
            this.radiusRange = radiusRange;

            //initialize the array
            houghSpace = new double[width][height][radiusRange];
        }

    }

    private double scaleValueBetween(double value, int from, int to, int min, int max) {
        return (to - from) * ((value - min) / (max - min)) + from;
    }


} //class HoughTransformIrisDetectionTemplate_

 