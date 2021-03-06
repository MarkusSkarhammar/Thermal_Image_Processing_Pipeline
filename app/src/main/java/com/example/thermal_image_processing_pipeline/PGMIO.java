package com.example.thermal_image_processing_pipeline;

/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2015 Arman Bilge
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static com.example.thermal_image_processing_pipeline.MainActivity.str_h;
import static com.example.thermal_image_processing_pipeline.MainActivity.str_w;

/**
 * A utility class for reading and writing PGM images. Methods use integers to represent unsigned bytes.
 *
 * Does not fully conform to the PGM specification because currently there is no support for:
 * <ul>
 *     <li>More than one image per file</li>
 *     <li>Images with more than 256 shades of gray</li>
 *     <li>Comments within the raster</li>
 * </ul>
 *
 * @author Arman Bilge
 */
public final class PGMIO {

    /**
     * Magic number representing the binary PGM file type.
     */
    private static final String MAGIC = "P5";
    /**
     * Character indicating a comment.
     */
    private static final char COMMENT = '#';
    /**
     * The maximum gray value.
     */
    private static final int MAXVAL = 65536;

    /**
     *  Read two bytes per stride
     */
    private static final int TWOBYTES = 2;

    private PGMIO() {}

    /**
     * Reads a grayscale image from a file in PGM format.
     * @param file the PGM file read from
     * @return two-dimensional byte array representation of the image
     * @throws IOException
     */
    public static PGMImage read(final File file) throws IOException {
        int amount = 1;
        final BufferedInputStream stream = new BufferedInputStream(new FileInputStream(file));
        try {
            if (!next(stream).equals(MAGIC))
                throw new IOException("File " + file + " is not a binary PGM image.");
            final int row = Integer.parseInt(next(stream));
            final int col = Integer.parseInt(next(stream));
            final int max = Integer.parseInt(next(stream));
            if (max < 0 || max > MAXVAL)
                throw new IOException("The image's maximum gray value must be in range [0, " + MAXVAL + "].");
            if (max > 255)
                amount = 2;

            /*
            final int[][] image = new int[col][row];
            for (int j = 0; j < col; ++j) {
                for (int i = 0; i < row; ++i) {
                    final int p = getData(stream, amount);
                    if (p == -1)
                        throw new IOException("Reached end-of-file prematurely.");
                    else if (p < 0 || p > MAXVAL)
                        throw new IOException("Pixel value " + p + " outside of range [0, " + max + "].");
                    image[j][i] = p;
                }
            }
            */

            /*int[]image = new int[col * row];
            int dataIndex = 0;
            int temp, b1, b2 = 0, b3 = 0;

            for(int h=0; h<str_h;h++)
                for(int w=0;w<str_w;w++) {

                    if (dataIndex % 3 == 0) {
                        b1 = getData(stream, 1) & 0xff;
                        b2 = getData(stream, 1) & 0xff;
                        b3 = getData(stream, 1) & 0xff;
                        temp = ((b2 & 0xf) << 8) | b1;
                        dataIndex += 1;
                    } else {
                        temp = (b2 >> 4) | (b3 << 4);
                        dataIndex += 2;
                    }
                    image[(h*str_w) + w] = temp;
                }
            */

            final int[]image = new int[col * row];
            for (int i = 0; i < (col * row); ++i) {
                final int p = getData(stream, amount);
                if (p == -1)
                    throw new IOException("Reached end-of-file prematurely.");
                else if (p < 0 || p > MAXVAL)
                    throw new IOException("Pixel value " + p + " outside of range [0, " + max + "].");
                image[i] = p;
            }

            return new PGMImage(image, image);
        } finally {
            stream.close();
        }
    }

    /**
     * Finds the next whitespace-delimited string in a stream, ignoring any comments.
     * @param stream the stream read from
     * @return the next whitespace-delimited string
     * @throws IOException
     */
    private static String next(final InputStream stream) throws IOException {
        final List<Byte> bytes = new ArrayList<Byte>();
        while (true) {
            final int b = stream.read();

            if (b != -1) {

                final char c = (char) b;
                if (c == COMMENT) {
                    int d;
                    do {
                        d = stream.read();
                    } while (d != -1 && d != '\n' && d != '\r');
                } else if (!Character.isWhitespace(c)) {
                    bytes.add((byte) b);
                } else if (bytes.size() > 0) {
                    break;
                }

            } else {
                break;
            }

        }
        final byte[] bytesArray = new byte[bytes.size()];
        for (int i = 0; i < bytesArray.length; ++i)
            bytesArray[i] = bytes.get(i);
        return new String(bytesArray);
    }

    private static int getData(final InputStream stream, int amount) throws IOException{
        final ArrayList<Integer> bytes = new ArrayList<Integer>();
        while(bytes.size() < amount){
            final int b = stream.read();

            if(b != -1){
                bytes.add(b);
            }
        }
        if(bytes.size()>1){
            int c = bytes.get(0) << 8;
            c = (c | bytes.get(1));
            return c;
        }else
            return bytes.get(0);
    }

    /**
     * Writes a grayscale image to a file in PGM format.
     * @param image a two-dimensional byte array representation of the image
     * @param file the file to write to
     * @throws IOException
     */
    public static void write(final int[][] image, final File file) throws IOException {
        write(image, file, MAXVAL);
    }


    public static void write(PGMImage img) throws IOException {
        int[][] image = new int[str_h][str_w];
        int[] data = img.getDataList();
        for(int h = 0; h < str_h; h++)
            for(int w = 0; w < str_w; w++){
                image[h][w] = data[(h * str_w) + w];
            }
        write(image, FileManagement.createEmptyPGMFile(MainActivity.activity, "image_" + System.currentTimeMillis()), img.getMaxValue());
    }

    public static void write(PGMImage img, boolean useRaw) throws IOException {
        int[][] image = new int[str_h][str_w];
        int[] data = img.getDataListRaw();
        for(int h = 0; h < str_h; h++)
            for(int w = 0; w < str_w; w++){
                image[h][w] = data[(h * str_w) + w];
            }
        write(image, FileManagement.createEmptyPGMFile(MainActivity.activity, "image_" + System.currentTimeMillis()), img.getMaxValue());
    }

    /**
     * Writes a grayscale image to a file in PGM format.
     * @param image a two-dimensional byte array representation of the image
     * @param file the file to write to
     * @param maxval the maximum gray value
     * @throws IOException
     */
    public static void write(final int[][] image, final File file, final int maxval) throws IOException {
        if (maxval > MAXVAL)
            throw new IllegalArgumentException("The maximum gray value cannot exceed " + MAXVAL + ".");
        final BufferedOutputStream stream = new BufferedOutputStream(new FileOutputStream(file));
        int c = 0;
        try {
            stream.write(MAGIC.getBytes());
            stream.write("\n".getBytes());
            stream.write(Integer.toString(image[0].length).getBytes());
            stream.write(" ".getBytes());
            stream.write(Integer.toString(image.length).getBytes());
            stream.write("\n".getBytes());
            stream.write(Integer.toString(maxval).getBytes());
            stream.write("\n".getBytes());

            for (int i = 0; i < image.length; ++i) {
                for (int j = 0; j < image[0].length; ++j) {

                    final int p = image[i][j];

                    if (maxval < 256) {     // One byte will fit.
                        stream.write(p);
                    } else {                // Two bytes necessary.
                        if (p < 256) {      // Check if padding is needed.
                            stream.write(0x0);
                            stream.write(p & 0xFF);
                        } else {
                            stream.write((p & 0xFF00) >> 8);    // Most significant byte first.
                            stream.write(p & 0xFF);
                        }
                    }
                }
            }
        } finally {
            stream.close();
        }
    }

}
