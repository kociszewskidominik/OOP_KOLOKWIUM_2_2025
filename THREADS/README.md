# WĄTKI
### Klasa Main
```java
package org.example;

import java.awt.image.BufferedImage;
import java.util.concurrent.ExecutionException;

public class Main {
    public static void main(String[] args) {
        // Tworzy menedżera obrazów
        ImageManager mgr = new ImageManager();

        // Nazwa pliku wejściowego (w zasobach projektu)
        String inputResource = "piespawacz.png";

        try {
            // Wczytanie obrazu z pliku
            mgr.loadImage(inputResource);

            // Pomiar czasu dla wersji sekwencyjnej
            long t0 = System.nanoTime();
            BufferedImage seq = mgr.increaseBrightness(50f);  // zwiększ jasność o 50 jednostek
            long t1 = System.nanoTime();
            mgr.saveImage(seq, "out_seq.png");                // zapisz wynik

            // Test równoległy (podział na wątki)
            mgr.loadImage(inputResource);                     // wczytaj ponownie oryginał
            long t2 = System.nanoTime();
            BufferedImage par = mgr.increaseBrightnessParallel(50f);
            long t3 = System.nanoTime();
            mgr.saveImage(par, "out_par.png");

            // Test wersji z pulą wątków (ThreadPool)
            mgr.loadImage(inputResource);                     // znowu oryginał
            long t4 = System.nanoTime();
            BufferedImage pool = mgr.increaseBrightnessThreadPool(50f);
            long t5 = System.nanoTime();
            mgr.saveImage(pool, "out_pool.png");

            // Wyświetl porównanie czasów
            System.out.printf(
                    "seq=%dms par=%dms pool=%dms%n",
                    (t1 - t0) / 1_000_000,
                    (t3 - t2) / 1_000_000,
                    (t5 - t4) / 1_000_000
            );

            // Histogram kanału czerwonego
            mgr.loadImage(inputResource);                   // oryginał
            int[] hist = mgr.computeHistogram(Channel.RED); // histogram RED
            BufferedImage histImg = mgr.drawHistogram(hist);
            mgr.saveImage(histImg, "hist_red.png");         // zapis wykresu histogramu

        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }
} 
```
### Klasa ImageManager
```java
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
    // Obraz aktualnie wczytany do pamięci
    private BufferedImage image;

    // Wczytuje obraz z zasobów projektu
    public void loadImage(String resourceName) {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(resourceName)) {
            if (is == null) {
                throw new RuntimeException("Resource not found: " + resourceName);
            }
            this.image = ImageIO.read(is); // odczyt pliku jako BufferedImage
        } catch (IOException e) {
            throw new RuntimeException("Failed to load image: " + resourceName, e);
        }
    }

    // Zapisuje obraz do pliku PNG
    public void saveImage(BufferedImage imageToSave, String path) {
        try {
            ImageIO.write(imageToSave, "png", new File(path));
        } catch (IOException e) {
            throw new RuntimeException("Failed to save image: " + path, e);
        }
    }

    // Zwiększa jasność sekwencyjnie
    public BufferedImage increaseBrightness(float offset) {
        RescaleOp op = new RescaleOp(1.0f, offset, null); // współczynnik skali=1, przesunięcie=offset
        op.filter(image, image);                          // filtruje obraz in-place
        return image;
    }

    // Zwiększa jasność – równolegle, dzieląc obraz na paski
    public BufferedImage increaseBrightnessParallel(float offset) throws InterruptedException {
        int cores = Runtime.getRuntime().availableProcessors(); // liczba rdzeni
        int w     = image.getWidth();
        int h     = image.getHeight();
        int stripe = h / cores;                                // wysokość jednego paska

        List<Thread> threads = new ArrayList<>(cores);
        for (int i = 0; i < cores; i++) {
            int y = i * stripe;
            int height = (i == cores - 1) ? (h - y) : stripe;
            BufferedImage sub = image.getSubimage(0, y, w, height); // fragment obrazu

            // Tworzy i uruchamia wątek
            Thread t = new Thread(() -> {
                RescaleOp op = new RescaleOp(1.0f, offset, null);
                op.filter(sub, sub);
            });
            threads.add(t);
            t.start();
        }

        // Czekaj na zakończenie wszystkich wątków
        for (Thread t : threads) {
            t.join();
        }
        return image;
    }

    // Zwiększa jasność używając puli wątków (jedna linia = jedno zadanie)
    public BufferedImage increaseBrightnessThreadPool(float offset) throws InterruptedException {
        int cores = Runtime.getRuntime().availableProcessors();
        int w     = image.getWidth();
        int h     = image.getHeight();

        ExecutorService pool = Executors.newFixedThreadPool(cores);
        for (int y = 0; y < h; y++) {
            final int yy = y;
            BufferedImage row = image.getSubimage(0, yy, w, 1);

            // Dodaje zadanie do puli
            pool.submit(() -> {
                RescaleOp op = new RescaleOp(1.0f, offset, null);
                op.filter(row, row);
            });
        }
        pool.shutdown(); // nie przyjmuje nowych zadań
        pool.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS); // czeka na zakończenie
        return image;
    }

    // Liczy histogram (zlicza wartości kanału)
    public int[] computeHistogram(Channel channel) throws InterruptedException, ExecutionException {
        int w = image.getWidth();
        int h = image.getHeight();
        int cores = Runtime.getRuntime().availableProcessors();
        int block = h / cores;

        ExecutorService pool = Executors.newFixedThreadPool(cores);
        List<Future<int[]>> futures = new ArrayList<>(cores);

        // Każdy wątek liczy fragment obrazu
        for (int i = 0; i < cores; i++) {
            int y = i * block;
            int height = (i == cores - 1) ? (h - y) : block;
            futures.add(pool.submit(() -> {
                int[] hist = new int[256];                 // 256 wartości jasności
                int shift = switch (channel) {             // wybór kanału koloru
                    case RED -> 16;
                    case GREEN -> 8;
                    case BLUE -> 0;
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

        // Sumowanie częściowych wyników
        int[] result = new int[256];
        for (Future<int[]> f : futures) {
            int[] part = f.get();
            for (int i = 0; i < 256; i++) {
                result[i] += part[i];
            }
        }
        return result;
    }

    // Rysuje histogram jako obrazek
    public BufferedImage drawHistogram(int[] hist) {
        int w = 256, h = 200;
        BufferedImage out = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = out.createGraphics();

        // Znajduje największą wartość – skalowanie słupków
        int max = 0;
        for (int v : hist) {
            if (v > max) max = v;
        }

        // Rysowanie słupków
        for (int i = 0; i < 256; i++) {
            int bar = (max == 0) ? 0 : (hist[i] * h / max);
            g.fillRect(i, h - bar, 1, bar);
        }
        g.dispose();
        return out;
    }
}
```
### Klasa Channel
```java
package org.example;

public enum Channel {
    RED,    // kanał czerwony
    GREEN,  // kanał zielony
    BLUE    // kanał niebieski
}
```
