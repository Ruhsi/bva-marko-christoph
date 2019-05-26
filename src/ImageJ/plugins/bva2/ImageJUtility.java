import ij.ImagePlus;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;

import java.awt.*;

public class ImageJUtility {

    public static int[][] convertFrom1DByteArr(byte[] pixels, int width, int height) {

        int[][] inArray2D = new int[width][height];

        int pixelIdx1D = 0;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                inArray2D[x][y] = (int) pixels[pixelIdx1D];
                if (inArray2D[x][y] < 0) {
                    inArray2D[x][y] += 256;
                } // if
                pixelIdx1D++;
            }
        }

        return inArray2D;
    }


    public static double[][] convertToDoubleArr2D(int[][] inArr, int width, int height) {
        double[][] returnArr = new double[width][height];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                returnArr[x][y] = inArr[x][y];
            }
        }

        return returnArr;
    }

    public static int[][] convertToIntArr2D(double[][] inArr, int width, int height) {
        int[][] returnArr = new int[width][height];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                returnArr[x][y] = (int) (inArr[x][y] + 0.5);
            }
        }

        return returnArr;
    }


    public static byte[] convertFrom2DIntArr(int[][] inArr, int width, int height) {
        int pixelIdx1D = 0;
        byte[] outArray2D = new byte[width * height];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int resultVal = inArr[x][y];
                if (resultVal > 127) {
                    resultVal -= 256;
                }
                outArray2D[pixelIdx1D] = (byte) resultVal;
                pixelIdx1D++;
            }
        }

        return outArray2D;
    }

    public static void showNewImage(int[][] inArr, int width, int height, String title) {
        byte[] byteArr = ImageJUtility.convertFrom2DIntArr(inArr, width, height);
        ImageJUtility.showNewImage(byteArr, width, height, title);
    }

    public static void showNewImage(double[][] inArr, int width, int height, String title) {
        int[][] intArr = ImageJUtility.convertToIntArr2D(inArr, width, height);
        byte[] byteArr = ImageJUtility.convertFrom2DIntArr(intArr, width, height);
        ImageJUtility.showNewImage(byteArr, width, height, title);
    }

    public static void showNewImage(byte[] inByteArr, int width, int height, String title) {
        ImageProcessor outImgProc = new ByteProcessor(width, height);
        outImgProc.setPixels(inByteArr);

        ImagePlus ip = new ImagePlus(title, outImgProc);
        ip.show();
    }

    public static double[][] cropImage(double[][] inImg, int width, int height, Rectangle roi) {
        int roiWidth = roi.width;
        int roiHeight = roi.height;

        int roiXseed = roi.x;
        int roiYseed = roi.y;

        double[][] returnImg = new double[roiWidth][roiHeight];
        for (int xIdx = 0; xIdx < width; xIdx++) {
            for (int yIdx = 0; yIdx < height; yIdx++) {
                int origXIdx = xIdx + roiXseed;
                int origYIdx = yIdx + roiYseed;
                returnImg[xIdx][yIdx] = inImg[origXIdx][origYIdx];
            }
        }

        return returnImg;
    }

    public static int CONVERSION_MODE_RGB_GRAYSCALE_MEAN = 1;
    public static int CONVERSION_MODE_RGB_GRAYSCALE_WEIGHTED = 2;

    public static double[][] getGrayscaleImgFromRGB(ImageProcessor imgProc, int conversionMode) {
        int width = imgProc.getWidth();
        int height = imgProc.getHeight();
        int[] rgbArr = new int[3];
        double[][] returnImg = new double[imgProc.getWidth()][imgProc.getHeight()];
        for (int xIdx = 0; xIdx < width; xIdx++) {
            for (int yIdx = 0; yIdx < height; yIdx++) {
                rgbArr = imgProc.getPixel(xIdx, yIdx, rgbArr);
                if (conversionMode == CONVERSION_MODE_RGB_GRAYSCALE_MEAN) {
                    double meanVal = rgbArr[0] + rgbArr[1] + rgbArr[2];
                    meanVal = meanVal / 3.0;
                    returnImg[xIdx][yIdx] = meanVal;
                } else if(conversionMode == CONVERSION_MODE_RGB_GRAYSCALE_WEIGHTED){
                    double weightedValue = 0.299 * rgbArr[0] + 0.587 * rgbArr[1] + 0.114 * rgbArr[2];
                    returnImg[xIdx][yIdx] = weightedValue;
                }
            }
        }

        return returnImg;
    }

    public static int[][] calculateImgDifference(int[][] inImgA, int[][] inImgB, int width, int height) {
        int[][] returnImg = new int[width][height];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                returnImg[x][y] = Math.abs(inImgA[x][y] - inImgB[x][y]);
            }
        }

        return returnImg;
    }

    static double[][] SOBELH = new double[][]{{1.0, 0.0, -1.0}, {2.0, 0.0, -2.0}, {1.0, 0.0, -1.0}};
    static double[][] SOBELV = new double[][]{{1.0, 2.0, 1.0}, {0.0, 0.0, 0.0}, {-1.0, -2.0, -1.0}};

    public static double[][] sobel(ImageProcessor ip, double[][] inDataArrDbl, double[][] kernel) {
        int width = ip.getWidth();
        int height = ip.getHeight();

        //ConvolutionFilter.GetMeanMask(tgtRadius);

        //apply kernel for convolution
        double[][] resultImg = ConvolutionFilter.ConvolveDouble(inDataArrDbl, width, height, kernel, 1);

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

        return resultImg;
    }

    private static int numberOfIterations = 20;
    private static int kappa = 25;
    public static int[][] anisotropicDiffusion(double[][] inDataArrDouble, int width, int height) {
        int maskSize = 3;
        int[][] resultImg = new int[width][height];
        int[][] newImage = ImageJUtility.convertToIntArr2D(inDataArrDouble, width, height);
        ;
        double[][] distanceWeightMask = createdistanceWeightMask();
        double meanIntensityValue = 0.0;

        for (int n = 0; n < numberOfIterations; n++) {
            meanIntensityValue = 0.0;
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    double sum = 0.0;
                    double delta = 0.0;
                    try {
                        for (int i = 0; i < maskSize; i++) {
                            for (int j = 0; j < maskSize; j++) {
                                if (i == 1 && j == 1) {
                                    // nothing to do
                                    // ignore middle value
                                } else {
                                    double coeffvalue = createDiffCoefficientValue(
                                            newImage[x][y],
                                            newImage[x - (maskSize / 2) + i]
                                                    [y - (maskSize / 2) + j]);
                                    double distanceWeightValue = distanceWeightMask[i][j];
                                    double nablaValue = newImage[x - (maskSize / 2) + i]
                                            [y - (maskSize / 2) + j] - newImage[x][y];

                                    sum += nablaValue * distanceWeightValue * coeffvalue;
                                }
                            }
                        }
                        meanIntensityValue += newImage[x][y];


                        delta += (1 / Math.pow(1, 2)) * (
                                createDiffCoefficientValue(newImage[x][y], newImage[x + 1][y]) * (newImage[x + 1][y] - newImage[x][y]) +
                                        createDiffCoefficientValue(newImage[x][y], newImage[x - 1][y]) * (newImage[x - 1][y] - newImage[x][y])
                        ); // 1 + 5
                        delta += (1 / Math.pow(1, 2)) * (
                                createDiffCoefficientValue(newImage[x][y], newImage[x][y - 1]) * (newImage[x][y - 1] - newImage[x][y]) +
                                        createDiffCoefficientValue(newImage[x][y], newImage[x][y + 1]) * (newImage[x][y + 1] - newImage[x][y])
                        ); // 3 + 7
                        delta += (1 / Math.pow(Math.sqrt(2), 2)) * (
                                createDiffCoefficientValue(newImage[x][y], newImage[x - 1][y - 1]) * (newImage[x - 1][y - 1] - newImage[x][y]) +
                                        createDiffCoefficientValue(newImage[x][y], newImage[x + 1][y + 1]) * (newImage[x + 1][y + 1] - newImage[x][y])
                        ); // 4 + 8
                        delta += (1 / Math.pow(Math.sqrt(2), 2)) * (
                                createDiffCoefficientValue(newImage[x][y], newImage[x + 1][y - 1]) * (newImage[x + 1][y - 1] - newImage[x][y]) +
                                        createDiffCoefficientValue(newImage[x][y], newImage[x - 1][y + 1]) * (newImage[x - 1][y + 1] - newImage[x][y])
                        ); // 2 + 6

                        double normFactor = 0;
                        if (newImage[x][y] != 0) {
                            normFactor = 1 / (newImage[x][y] * Math.sqrt(2) + newImage[x][y]);
                        }

                        double newValue = newImage[x][y] + normFactor * (sum + delta);
                        resultImg[x][y] = (int) (newValue);
                    } catch (IndexOutOfBoundsException e) {
                        resultImg[x][y] = newImage[x][y];
                    }
                }
            }
            meanIntensityValue = meanIntensityValue / (width * height);
            newImage = resultImg;
        }
        return resultImg;
    }

    private static double[][] createdistanceWeightMask() {
        double distanceValue = 1 / Math.sqrt(2);
        double[][] distanceWeight = new double[][]{
                {distanceValue, 1, distanceValue},
                {1, -1, 1},
                {distanceValue, 1, distanceValue}
        };
        return distanceWeight;
    }

    private static double createDiffCoefficientValue(int middleValue, int outerValue) {
        double diffCoeff = Math.exp(-1 *
                ((Math.pow(Math.abs(middleValue - outerValue), 2)) /
                        (2 * Math.pow(kappa, 2))));
        return diffCoeff;
    }

}
