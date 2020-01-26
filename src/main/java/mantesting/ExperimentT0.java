package mantesting;

import solver.*;
import ttp.TTP1Instance;
import ttp.TTPSolution;
import utils.CalAvg;
import utils.Deb;
import utils.Quicksort;
import utils.TwoOptHelper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.*;

/**
 * junk testing
 */
public class ExperimentT0 {

    public static void main(String[] args) {

        // test 3 instances(group by KP data types)
        String[] exp_list = { "eil76_n75_bounded-strongly-corr_01.ttp", "eil76_n225_uncorr_01.ttp",
                "eil76_n75_uncorr-similar-weights_01.ttp" };

        // T0 test list
        double[] T0_list = { 50, 55, 60, 65, 70, 75, 80, 85, 90, 95, 100, 105, 110, 115, 120, 125, 130, 135, 140, 145,
                150, 155, 160, 165, 170, 175, 180, 185, 190, 195, 200, 205, 210, 215, 220, 225, 230, 235, 240, 245, 250,
                255, 260, 265, 270, 275, 280, 285, 290, 295, 300 };

        // test 20 times
        // result array
        long[] ob_result = new long[20];
        double[] exTime_result = new double[20];

        for (int exp_iter = 0; exp_iter < exp_list.length; exp_iter++) {
            String exp = exp_list[exp_iter];

            // TTP instance name
            final String inst = exp;

            String[] spl = exp.split("_", 2);

            // output file
            final String outputFile = "./output/CS2SA-T0-experiment.csv";

            // runtime limit(big enough to run)
            long runtimeLimit = Long.MAX_VALUE;

            // TTP instance dir
            final TTP1Instance ttp = new TTP1Instance(spl[0] + "-ttp/" + inst);

            // test every T0
            for (int T0_iter = 0; T0_iter < T0_list.length; T0_iter++) {
                double t = T0_list[T0_iter];
                final SearchHeuristic algo = new CS2SA(ttp, t);

                // runnable class
                class TTPRunnable implements Runnable {

                    String resultLine;

                    @Override
                    public void run() {

                        /* start search & measure runtime */
                        long startTime, stopTime;
                        long exTime;

                        // every instance will be calculated 20 times
                        // then calculate avg of ob and extime
                        for (int i = 0; i < 20; i++) {
                            startTime = System.currentTimeMillis();

                            TTPSolution sx = algo.search();

                            stopTime = System.currentTimeMillis();
                            exTime = stopTime - startTime;

                            ob_result[i] = Math.round(sx.ob);
                            exTime_result[i] = exTime / 1000.0;
                        }

                        /* print result */
                        resultLine = inst + " " + t + " " + Math.round(CalAvg.calculateAvg(ob_result)) + " "
                                + (CalAvg.calculateAvg(exTime_result));

                    }
                }
                ;

                // my TTP runnable
                TTPRunnable ttprun = new TTPRunnable();
                ExecutorService executor = Executors.newFixedThreadPool(4);
                Future<?> future = executor.submit(ttprun);
                executor.shutdown(); // reject all further submissions

                try {
                    future.get(runtimeLimit, TimeUnit.SECONDS); // wait X seconds to finish
                } catch (InterruptedException e) {
                    System.out.println("job was interrupted");
                } catch (ExecutionException e) {
                    System.out.println("caught exception: " + e.getCause());
                } catch (TimeoutException e) {
                    future.cancel(true);
                    System.out.println("/!\\ Timeout");
                }

                // wait for execution to be done
                try {
                    executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                // print results
                Deb.echo(ttprun.resultLine);

                // log results into text file
                try {
                    File file = new File(outputFile);
                    if (!file.exists())
                        file.createNewFile();
                    Files.write(Paths.get(outputFile), (ttprun.resultLine + "\n").getBytes(),
                            StandardOpenOption.APPEND);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }

    }

}