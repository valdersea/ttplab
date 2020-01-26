package utils;

/**
 * calculate avg
 * 
 * @author valdersea
 * 
 */
public class CalAvg {

    // calculate avg of ob
    public static long calculateAvg(long[] arr) {
        long sum = 0, avg;
        for (int i = 0; i < arr.length; i++) {
            sum += arr[i];
        }
        avg = sum / arr.length;

        return avg;
    }

    // calculate avg of extime
    public static double calculateAvg(double[] arr) {
        double sum = 0, avg;
        for (int i = 0; i < arr.length; i++) {
            sum += arr[i];
        }
        avg = sum / arr.length;

        return avg;
    }
}