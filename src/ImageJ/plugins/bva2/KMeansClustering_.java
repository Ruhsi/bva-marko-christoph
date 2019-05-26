import ij.IJ;
import ij.ImagePlus;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;

import java.util.Vector;

public class KMeansClustering_ implements PlugInFilter {
 
	public int setup(String arg, ImagePlus imp) {
		if (arg.equals("about"))
			{showAbout(); return DONE;}
		return DOES_RGB+DOES_STACKS+SUPPORTS_MASKING;
	} //setup

	
	public void run(ImageProcessor ip) {
		
		double[] blackCluster = new double[] {0, 0, 0};
		double[] redCluster = new double[] {255, 0, 0};
		double[] blueCluster = new double[] {0, 0, 255};
		double[] greenCluster = new double[] {0, 255, 0};
		Vector<double[]> clusterCentroides = new Vector<double[]>();
		clusterCentroides.add(blackCluster);
		clusterCentroides.add(redCluster);
		clusterCentroides.add(greenCluster);
		clusterCentroides.add(blueCluster);
	    int numOfIterations = 5;
        
		for(int i = 0; i < numOfIterations; i++) {
			clusterCentroides = UpdateClusters(ip, clusterCentroides);
		}
		
		//TODO: calculate rounded centroide colors
		
		//TODO: get best cluster for each pixel for a last time ==> apply cluster color
		//int[] rgbArr = new int[3];
		//get color: ip.getPixel(x,  y, rgbArr);
		//set color: ip.putPixel(x,  y,  colorsToApply);
			
		
		
	} //run
	
	Vector<double[]> UpdateClusters(ImageProcessor ip, Vector<double[]> inClusters) {
	   	//TODO: implementation required
		return null;
	}
	
	double ColorDist(double[] refColor, int[] currColor) {
		//TODO: implementation required
		return -1;
	}
	
	int GetBestCluster(int[] rgbArr, Vector<double[]> clusters) {
		//TODO: implementation required
		return -1;
	}
		

	void showAbout() {
		IJ.showMessage("About KMeansClusteringTemplate_...",
			"this is a PluginFilter template\n");
	} //showAbout
	
} //class KMeansClusteringTemplate_