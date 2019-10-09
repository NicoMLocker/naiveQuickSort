import java.io.FileWriter;
import java.io.PrintWriter;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.Arrays;
import java.util.Random;

public class NaiveQuickSort {

    static ThreadMXBean bean = ManagementFactory.getThreadMXBean( );

    /* define constants */
    static long MAXVALUE =  1000;
    static long MINVALUE = -1000;
    static int numberOfTrials = 10;  // can change 1-1000 for testing , set at 1000 for actual test
    static int MAXINPUTSIZE  = (int) Math.pow(2,20);  // can change from 29 to 10-20 for testing
    static int MININPUTSIZE  =  1;

    // static int SIZEINCREMENT =  10000000; // not using this since we are doubling the size each time
    static String ResultsFolderPath = "/home/nicolocker/Results/"; // pathname to results folder
    static FileWriter resultsFile;
    static PrintWriter resultsWriter;
    static double[] avgTimeList = new double[2];
    static double ratio = 0;

    public static void main(String[] args) {

        verifySorted();

        // run the whole experiment at least twice, and expect to throw away the data from the earlier runs, before java has fully optimized
        System.out.println("Running first full experiment...");
        runFullExperiment("NaiveQuickSort-Exp1.txt");
        System.out.println("Running second full experiment...");
        runFullExperiment("NaiveQuickSort-Exp2.txt");
        System.out.println("Running third full experiment...");
        runFullExperiment("NaiveQuickSort-Exp3.txt");
    }

    public static long[] createRandomIntegerList(int size){
        long[] newList = new long[size];
        for(int j = 0; j < size; j++){
            newList[j] = (long)(MAXVALUE + Math.random() *  (MAXVALUE - MINVALUE));
        }
        return newList;
    }

    public static void verifySorted(){
        long[] testList1 = {1,2,3,4,5,6, 0, -22, -100};
        System.out.println("\nTest List 1: " + Arrays.toString(testList1));
        quickSort(testList1);
        System.out.println("Sorted List 1: " + Arrays.toString(testList1) + "\n");

        long[] testList2 = {324110,-442472,626686,-157678,508681,123414,-77867,155091,129801,287381};
        System.out.println("Test List 2: " + Arrays.toString(testList2));
        quickSort(testList2);
        System.out.println("Sorted List 2: " + Arrays.toString(testList2) + "\n");

        long[] testList3 = {1,2,6, -3, 12, -18};
        System.out.println("Test List 3: " + Arrays.toString(testList3));
        quickSort(testList3);
        System.out.println("Sorted List 3: " + Arrays.toString(testList3) + "\n");
    }


    public static void runFullExperiment(String resultsFileName){

        try {
            resultsFile = new FileWriter(ResultsFolderPath + resultsFileName);
            resultsWriter = new PrintWriter(resultsFile);
        } catch(Exception e) {
            System.out.println("*****!!!!!  Had a problem opening the results file "+ResultsFolderPath+resultsFileName);
            return; // not very foolproof... but we do expect to be able to create/open the file...
        }

        ThreadCpuStopWatch BatchStopwatch = new ThreadCpuStopWatch(); // for timing an entire set of trials
        ThreadCpuStopWatch TrialStopwatch = new ThreadCpuStopWatch(); // for timing an individual trial

        resultsWriter.println("#InputSize       AverageTime              Ratio"); // # marks a comment in gnuplot data
        resultsWriter.flush();
        /* for each size of input we want to test: in this case starting small and doubling the size each time */

        for(int inputSize=MININPUTSIZE;inputSize<=MAXINPUTSIZE; inputSize*=2) {
            // progress message...
            System.out.println("Running test for input size "+inputSize+" ... ");

            /* repeat for desired number of trials (for a specific size of input)... */
            long batchElapsedTime = 0;
            // generate a list of randomly spaced integers in ascending sorted order to use as test input
            // In this case we're generating one list to use for the entire set of trials (of a given input size)
            // but we will randomly generate the search key for each trial
            System.out.print("    Generating test data...");
            long[] testList = createRandomIntegerList(inputSize);
            System.out.println("...done.");
            System.out.print("    Running trial batch...");

            /* force garbage collection before each batch of trials run so it is not included in the time */
            System.gc();


            // instead of timing each individual trial, we will time the entire set of trials (for a given input size)
            // and divide by the number of trials -- this reduces the impact of the amount of time it takes to call the
            // stopwatch methods themselves
            //BatchStopwatch.start(); // comment this line if timing trials individually

            // run the tirals
            for (long trial = 0; trial < numberOfTrials; trial++) {
                // generate a random key to search in the range of a the min/max numbers in the list
                //    long testSearchKey = (long) (0 + Math.random() * (testList[testList.length-1]));
                /* force garbage collection before each trial run so it is not included in the time */
                // System.gc();

                TrialStopwatch.start(); // *** uncomment this line if timing trials individually
                /* run the function we're testing on the trial input */
                //    long foundIndex = binarySearch(testSearchKey, testList);

                NaiveQuickSort.quickSort(testList);

                batchElapsedTime = batchElapsedTime + TrialStopwatch.elapsedTime(); // *** uncomment this line if timing trials individually
            }

            //batchElapsedTime = BatchStopwatch.elapsedTime(); // *** comment this line if timing trials individually

            double averageTimePerTrialInBatch = (double) batchElapsedTime / (double)numberOfTrials; // calculate the average time per trial in this batch

            /* put the averageTimePerTrialBatch into an array for the ratio*/
            if(avgTimeList[0] == 0){
                avgTimeList[0] = averageTimePerTrialInBatch;  // set first averageTime to first spot in array
                //System.out.println("\nINDEX 0\n");  // used for testing
                //System.out.println(avgTimeList[0]); // used for testing
            } else{
                avgTimeList[1] = averageTimePerTrialInBatch; // set next averageTime to second spot in array
                //System.out.println("\nINDEX 0\n");  //used for testing
                //System.out.println(avgTimeList[0]); //used for testing
                //System.out.println("\nINDEX 1\n");  //used for testing
                //System.out.println(avgTimeList[1]);  //used for testing
                double ratio = avgTimeList[1]/avgTimeList[0];  // divide the larger time by the smaller time to get the ratio
                avgTimeList[0] = avgTimeList[1];              // set the current biggest averageTime to first array spot for next test
                //System.out.println(ratio);  //used for testing

                /* print data for this size of input */
                resultsWriter.printf("%12d  %15.2f %15.2f\n",inputSize, averageTimePerTrialInBatch, ratio); // might as well make the columns look nice
                resultsWriter.flush();
                System.out.println(" ....done.");
            }
        }
    }

    private static void quickSort(long[] list){
        quickSortWorker(list, 0, list.length - 1);
    }

    private static void quickSortWorker(long[] list, long lo, long hi){
        if (hi <= lo){
            return;
        }

        long pivot = partition(list, lo, hi);
        quickSortWorker(list, lo, pivot - 1);
        quickSortWorker(list, pivot + 1, hi);
    }

    // picks rightmost pivot
    private static long partition(long[] list, long lo, long hi){
        long pivot = list[(int) hi];

        for(int i = (int) lo; i < hi; i++){
            if(list[i] < pivot){
                long temp = list[(int) lo];
                list[(int) lo] = list[i];
                list[i] = temp;
                lo++;
            }
        }
        long temp = list[(int) lo];
        list[(int) lo] = pivot;
        list[(int) hi] = temp;

        return lo;
    }
}

