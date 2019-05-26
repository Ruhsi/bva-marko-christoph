
import ij.*;
import ij.plugin.filter.PlugInFilter;
import ij.process.*;

import java.awt.*;

import ij.gui.GenericDialog;


public class IntervalThresh_ implements PlugInFilter {

    public int setup(String arg, ImagePlus imp) {
        if (arg.equals("about")) {
            showAbout();
            return DONE;
        }
        return DOES_8G + DOES_STACKS + SUPPORTS_MASKING;
    } //setup


    public void run(ImageProcessor ip) {
        byte[] pixels = (byte[]) ip.getPixels();
        int width = ip.getWidth();
        int height = ip.getHeight();
        int[][] inDataArrInt = ImageJUtility.convertFrom1DByteArr(pixels, width, height);

        int FG_VAL = 255; //max value
        int BG_VAL = 0;
        int Tmin = 100;
        int Tmax = 240;

        //get transform array
        int[] tfArray = ImageTransformationFilter.GetBinaryThresholdTF(FG_VAL, Tmin, Tmax, FG_VAL, BG_VAL);
        int[][] resultImg = ImageTransformationFilter.GetTransformedImage(inDataArrInt, width, height, tfArray);

        ImageJUtility.showNewImage(resultImg, width, height, "inverted image");

    } //run

    void showAbout() {
        IJ.showMessage("About Template_...",
                "Inverts color values\n");
    } //showAbout

} //class Invert_

