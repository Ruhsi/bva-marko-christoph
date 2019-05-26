
import ij.*;
import ij.plugin.filter.PlugInFilter;
import ij.process.*;

import java.awt.*;

import ij.gui.GenericDialog;


public class Mean_ implements PlugInFilter {

    public int setup(String arg, ImagePlus imp) {
        if (arg.equals("about"))
        {showAbout(); return DONE;}
        return DOES_8G+DOES_STACKS+SUPPORTS_MASKING;
    } //setup



    public void run(ImageProcessor ip) {
        byte[] pixels = (byte[])ip.getPixels();
        int width = ip.getWidth();
        int height = ip.getHeight();
        int tgtRadius = 2;

        int[][] inDataArrInt = ImageJUtility.convertFrom1DByteArr(pixels, width, height);
        double[][] inDataArrDbl = ImageJUtility.convertToDoubleArr2D(inDataArrInt, width, height);


        //prepare kernel
        double[][] meanKernel = ConvolutionFilter.GetMeanMask(tgtRadius);

        //apply kernel for convolution
        double[][] resultImg = ConvolutionFilter.ConvolveDoubleNorm(inDataArrDbl, width, height, meanKernel, tgtRadius);

        ImageJUtility.showNewImage(resultImg, width, height, "mean with kernel r=" + tgtRadius);

    } //run

    void showAbout() {
        IJ.showMessage("About Template_...",
                "this is a PluginFilter template\n");
    } //showAbout

} //class FilterTemplate_

