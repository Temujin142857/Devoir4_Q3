import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.EnumSet;

public class Main {
    public static void main(String[] args) {
        int n = Integer.parseInt(args[0]);
        int[] catalan = generateCatalan(n);

        MappedByteBuffer shm = createSharedMemory("../shared.dat", 64 * 1024);
        for (int i = 0; i < n; i++) {
            shm.putInt(i * Integer.BYTES, catalan[i]);
        }

        shm.position(64 * 1024);

        runConsumer();
    }
    public static int[] generateCatalan(int n){
        int[] sequence=new int[n];
        for (int i = 1; i <= n; i++) {
            sequence[i-1]=factorial(2*i)/(factorial(i+1)*factorial(i));
        }
        return sequence;
    }

    public static int factorial(int n){
        if (n <= 2) {
            return n;
        }
        return n * factorial(n - 1);
    }

    static MappedByteBuffer createSharedMemory(String path, long size) {

        try {
            String absolutePath = Paths.get(System.getProperty("user.dir"), path).toString();
            File file = new File(absolutePath);

            // Create parent directories if they do not exist
            file.getParentFile().mkdirs();

            // Create the file if it doesn't exist
            if (!file.exists()) {
                file.createNewFile();
            }

            // Set the file size
            try (RandomAccessFile raf = new RandomAccessFile(file, "rw")) {
                raf.setLength(size);
            }

            // Map the file into memory
            FileChannel fc = (FileChannel) Files.newByteChannel(file.toPath(),
                    EnumSet.of(
                            StandardOpenOption.CREATE,
                            StandardOpenOption.SPARSE,
                            StandardOpenOption.WRITE,
                            StandardOpenOption.READ));

            return fc.map(FileChannel.MapMode.READ_WRITE, 0, size);
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }


    static void runConsumer() {
        try {
            String consumerClassPath = "../Devoir4_Q3_Consumer/src";
            ProcessBuilder pb = new ProcessBuilder("java", "-cp", consumerClassPath, "Main");
            pb.inheritIO(); // This makes the consumer process use the same I/O as the producer process
            Process process = pb.start();
            process.waitFor(); // Wait for the consumer process to finish
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

}