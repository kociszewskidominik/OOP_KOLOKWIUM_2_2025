package org.example;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class ImageManager {
    private BufferedImage image;

    public void loadImage(String resourceName) {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(resourceName)) {
            if (is == null) {
                throw new RuntimeException("Resource not found: " + resourceName);
            }
            this.image = ImageIO.read(is);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load image: " + resourceName, e);
        }
    }

    public void saveImage(BufferedImage imageToSave, String path) {
        try {
            ImageIO.write(imageToSave, "png", new File(path));
        } catch (IOException e) {
            throw new RuntimeException("Failed to save image: " + path, e);
        }
    }

    public BufferedImage increaseBrightness(float offset) {
        RescaleOp op = new RescaleOp(1.0f, offset, null);
        op.filter(image, image);
        return image;
    }

    public BufferedImage increaseBrightnessParallel(float offset) throws InterruptedException {
        int cores = Runtime.getRuntime().availableProcessors();
        int w     = image.getWidth();
        int h     = image.getHeight();
        int stripe = h / cores;

        List<Thread> threads = new ArrayList<>(cores);
        for (int i = 0; i < cores; i++) {
            int y      = i * stripe;
            int height = (i == cores - 1) ? (h - y) : stripe;
            BufferedImage sub = image.getSubimage(0, y, w, height);
            Thread t = new Thread(() -> {
                RescaleOp op = new RescaleOp(1.0f, offset, null);
                op.filter(sub, sub);
            });
            threads.add(t);
            t.start();
        }
        for (Thread t : threads) {
            t.join();
        }
        return image;
    }

    public BufferedImage increaseBrightnessThreadPool(float offset) throws InterruptedException {
        int cores = Runtime.getRuntime().availableProcessors();
        int w     = image.getWidth();
        int h     = image.getHeight();

        ExecutorService pool = Executors.newFixedThreadPool(cores);
        for (int y = 0; y < h; y++) {
            final int yy = y;
            BufferedImage row = image.getSubimage(0, yy, w, 1);
            pool.submit(() -> {
                RescaleOp op = new RescaleOp(1.0f, offset, null);
                op.filter(row, row);
            });
        }
        pool.shutdown();
        pool.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        return image;
    }

    public int[] computeHistogram(Channel channel) throws InterruptedException, ExecutionException {
        int w      = image.getWidth();
        int h      = image.getHeight();
        int cores  = Runtime.getRuntime().availableProcessors();
        int block  = h / cores;

        ExecutorService pool = Executors.newFixedThreadPool(cores);
        List<Future<int[]>> futures = new ArrayList<>(cores);
        for (int i = 0; i < cores; i++) {
            int y      = i * block;
            int height = (i == cores - 1) ? (h - y) : block;
            futures.add(pool.submit(() -> {
                int[] hist = new int[256];
                int shift = switch (channel) {
                    case RED   -> 16;
                    case GREEN -> 8;
                    case BLUE  -> 0;
                };
                for (int yy = y; yy < y + height; yy++) {
                    for (int xx = 0; xx < w; xx++) {
                        int v = (image.getRGB(xx, yy) >> shift) & 0xFF;
                        hist[v]++;
                    }
                }
                return hist;
            }));
        }
        pool.shutdown();
        pool.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);

        int[] result = new int[256];
        for (Future<int[]> f : futures) {
            int[] part = f.get();
            for (int i = 0; i < 256; i++) {
                result[i] += part[i];
            }
        }
        return result;
    }

    public BufferedImage drawHistogram(int[] hist) {
        int w = 256, h = 200;
        BufferedImage out = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = out.createGraphics();
        int max = 0;
        for (int v : hist) {
            if (v > max) max = v;
        }
        for (int i = 0; i < 256; i++) {
            int bar = (max == 0) ? 0 : (hist[i] * h / max);
            g.fillRect(i, h - bar, 1, bar);
        }
        g.dispose();
        return out;
    }
}
