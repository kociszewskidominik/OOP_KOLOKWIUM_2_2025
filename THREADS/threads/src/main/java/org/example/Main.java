package org.example;

import java.awt.image.BufferedImage;
import java.util.concurrent.ExecutionException;

public class Main {
    public static void main(String[] args) {
        ImageManager mgr = new ImageManager();
        String inputResource = "piespawacz.png";

        try {
            mgr.loadImage(inputResource);
            long t0 = System.nanoTime();
            BufferedImage seq = mgr.increaseBrightness(50f);
            long t1 = System.nanoTime();
            mgr.saveImage(seq, "out_seq.png");

            mgr.loadImage(inputResource);
            long t2 = System.nanoTime();
            BufferedImage par = mgr.increaseBrightnessParallel(50f);
            long t3 = System.nanoTime();
            mgr.saveImage(par, "out_par.png");

            mgr.loadImage(inputResource);
            long t4 = System.nanoTime();
            BufferedImage pool = mgr.increaseBrightnessThreadPool(50f);
            long t5 = System.nanoTime();
            mgr.saveImage(pool, "out_pool.png");

            System.out.printf(
                    "seq=%dms par=%dms pool=%dms%n",
                    (t1 - t0) / 1_000_000,
                    (t3 - t2) / 1_000_000,
                    (t5 - t4) / 1_000_000
            );

            mgr.loadImage(inputResource);
            int[] hist = mgr.computeHistogram(Channel.RED);
            BufferedImage histImg = mgr.drawHistogram(hist);
            mgr.saveImage(histImg, "hist_red.png");

        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }
}
