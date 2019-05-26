import ij.IJ;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.plugin.SurfacePlotter;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;
import javafx.beans.binding.DoubleExpression;


public class GaussFiltering_ implements PlugInFilter {

    private Double sigmaCoeff = 1.0; //calculated from input radius

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


        GenericDialog gd = createDialog();
        if (gd == null) return;
        double targetRadius = gd.getNextNumber();
        double numberOfIterations = gd.getNextNumber();
        System.out.println("target radius: " + targetRadius);
        System.out.println("number of iterations: " + numberOfIterations);

        int nSigma = (int) (targetRadius * 2 + 1);
        int kSigma = (nSigma - 1) / 2;
        sigmaCoeff = (Math.pow(nSigma + 1, 2) / (binomialCoeff(nSigma - 1, kSigma) * Math.sqrt(2 * Math.PI)));
        System.out.println(sigmaCoeff);
        double fixedPart = 1 / (2 * Math.PI * Math.pow(sigmaCoeff, 2));
        System.out.println("fixed:" + fixedPart);
        double[][] kernel = generateMask((int) targetRadius, targetRadius / sigmaCoeff, fixedPart);
        int kernelSize = (int) targetRadius * 2 + 1;

        int[][] resultImg = new int[width][height];
        int radius = (int) targetRadius;
        for (int n = 0; n < numberOfIterations; n++) {
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    double sum = 0.0;
                    for (int i = -radius; i < radius; i++) {
                        for (int j = -radius; j < radius; j++) {
                            try {
                                sum += inDataArrInt[(x - ((int) (kernelSize / 2)) + i)]
                                        [(y - ((int) (kernelSize / 2)) + j)] * kernel[i + radius][j + radius];
                            } catch (ArrayIndexOutOfBoundsException e) {

                            }
                        }
                    }
                    resultImg[x][y] = (int) sum;
                }
            }
        }
        ImageJUtility.showNewImage(resultImg, width, height, "Gauß Filter");

    } //run

    public GenericDialog createDialog() {
        GenericDialog gd = new GenericDialog("User Input");
        gd.addSlider("Target Radius", 1, 50, 3);
        gd.addSlider("Number of Iterations", 1, 200, 10);
        gd.showDialog();
        if (gd.wasCanceled()) {
            return null;
        } //if
        return gd;
    }

    public double[][] generateMask(int radius, double sigma, double fixedPart) {
        int size = radius * 2 + 1;
        double[][] kernel = new double[size][size];
        double sum = 0.0;
        for (int i = -radius; i <= radius; i++) {
            for (int j = -radius; j <= radius; j++) {
                kernel[i + radius][j + radius] = fixedPart * (Math.exp(-(Math.pow(i, 2) + Math.pow(j, 2)) / (2 * Math.pow(sigma, 2))));
                sum += kernel[i + radius][j + radius];
            }
        }

        //normalize mask
        double normSum = 0.0;
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                kernel[i][j] = kernel[i][j] / sum;
                normSum += kernel[i][j];
            }
        }

        drawKernel(kernel, size);

        // should be 1
        System.out.println("Sum of normalized kernel: " + normSum);


        return kernel;
    }

    public void drawKernel(double[][] kernel, int size) {
        double min = Double.MAX_VALUE;
        double max = Double.MIN_VALUE;

        int minRange = 0;
        int maxRange = 255;

        int[][] newKernel = new int[size][size];

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {

                if (kernel[i][j] < min) {
                    min = kernel[i][j];
                }
                if (kernel[i][j] > max) {
                    max = kernel[i][j];
                }
            }
        }
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                newKernel[i][j] = (int) ((maxRange - minRange) * ((kernel[i][j] - min) / (max - min)) + minRange);
            }
        }

        ImageJUtility.showNewImage(newKernel, size, size, "Gauß Mask");
    }

    void showAbout() {
        IJ.showMessage("About Template_...",
                "Inverts color values\n");
    } //showAbout


    public int binomialCoeff(int n, int k) {
        int C[][] = new int[n + 1][k + 1];
        int i, j;
        int min;

        // Caculate value of Binomial Coefficient in bottom up manner
        for (i = 0; i <= n; i++) {
            min = (i < k) ? i : k;

            for (j = 0; j <= min; j++) {
                // Base Cases
                if (j == 0 || j == i)
                    C[i][j] = 1;

                    // Calculate value using previosly stored values
                else
                    C[i][j] = C[i - 1][j - 1] + C[i - 1][j];
            }
        }

        return C[n][k];
    }


} //class Invert_

