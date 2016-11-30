package dmu.project.noise;
/*
 * OpenSimplex Noise sample class.
 */

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import dmu.project.levelgen.LevelGenerator;
import dmu.project.levelgen.PerlinLevelGen;

public class OpenSimplexNoiseTest {
    private static final int WIDTH = 1024;
    private static final int HEIGHT = 1024;

    public static void main(String[] args)
            throws IOException {

        OpenSimplexNoise noise = new OpenSimplexNoise();

        LevelGenerator gen = new PerlinLevelGen();
        double[][] level = gen.generateLevel(WIDTH, HEIGHT, WIDTH, HEIGHT);

        BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < HEIGHT; y++) {
            for (int x = 0; x < WIDTH; x++) {
                //double value = noise.eval(x / FEATURE_SIZE, y / FEATURE_SIZE, 0.0);
                int rgb = 0x010101 * (int) ((level[y][x]) * 255);//(int)((value + 1) * 127.5);
                image.setRGB(x, y, rgb);
            }
        }
        ImageIO.write(image, "png", new File("noise.png"));

    }
}