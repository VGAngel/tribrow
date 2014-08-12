package mythruna.util;

import java.io.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class SerializationUtils {
    public SerializationUtils() {
    }

    protected static void doWrite(Serializable o, File f, boolean zipped) throws IOException {
        FileOutputStream fOut = new FileOutputStream(f);
        ObjectOutputStream out;
        if (zipped) {
            GZIPOutputStream gOut = new GZIPOutputStream(fOut);
            BufferedOutputStream bOut = new BufferedOutputStream(gOut, 16384);
            out = new ObjectOutputStream(bOut);
        } else {
            BufferedOutputStream bOut = new BufferedOutputStream(fOut, 16384);
            out = new ObjectOutputStream(bOut);
        }

        try {
            out.writeObject(o);
        } finally {
            out.close();
        }
    }

    protected static Object doRead(File f, boolean zipped) throws IOException {
        FileInputStream fIn = new FileInputStream(f);
        ObjectInputStream in;
        if (zipped) {
            GZIPInputStream gIn = new GZIPInputStream(fIn);
            BufferedInputStream bIn = new BufferedInputStream(gIn, 16384);
            in = new ObjectInputStream(bIn);
        } else {
            BufferedInputStream bIn = new BufferedInputStream(fIn, 16384);
            in = new ObjectInputStream(bIn);
        }

        try {
            return in.readObject();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Class not found reading:" + f, e);
        } finally {
            in.close();
        }
    }

    public static void writeObject(Serializable o, File f) {
        writeObject(o, f, false);
    }

    public static void writeObject(Serializable o, File f, boolean zipped) {
        try {
            doWrite(o, f, zipped);
        } catch (IOException e) {
            throw new RuntimeException("Error writing object to:" + f, e);
        }
    }

    public static Object readObject(File f) {
        return readObject(f, false);
    }

    public static Object readObject(File f, boolean zipped) {
        try {
            return doRead(f, zipped);
        } catch (IOException e) {
            throw new RuntimeException("Error reading object from:" + f, e);
        }
    }
}