import ij.IJ;
import ij.ImagePlus;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;


public class Sobel_ implements PlugInFilter {

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
        double[][] inDataArrDbl = ImageJUtility.convertToDoubleArr2D(inDataArrInt, width, height);


        //prepare kernel
        double[][] sobelH = new double[][]{{1.0, 0.0, -1.0}, {2.0, 0.0, -2.0}, {1.0, 0.0, -1.0}};
        double[][] sobelV = new double[][]{{1.0, 2.0, 1.0}, {0.0, 0.0, 0.0}, {-1.0, -2.0, -1.0}};
        //ConvolutionFilter.GetMeanMask(tgtRadius);

        //apply kernel for convolution
        double[][] resultImg = ConvolutionFilter.ConvolveDouble(inDataArrDbl, width, height, sobelV, 1);

        //our edge results are somewhere in [-500, 500] ===> only use Abs and scale to [0;255]
        double maxVal = Double.MIN_VALUE;
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                double actVal = Math.abs(resultImg[x][y]);
                if (actVal > maxVal) {
                    maxVal = actVal;
                }
            }
        }

        double scaleFactor = 255.0 / maxVal;

        //apply normalization
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                resultImg[x][y] = Math.abs(resultImg[x][y]) * scaleFactor;
            }
        }

        ImageJUtility.showNewImage(resultImg, width, height, "sobel with kernel r=" + 1);

    } //run

    void showAbout() {
        IJ.showMessage("About Template_...",
                "this is a PluginFilter template\n");
    } //showAbout

} //class FilterTemplate_

