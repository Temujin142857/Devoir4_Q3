import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.StandardOpenOption;
import java.io.IOException;
import java.nio.file.Paths;

public class Main {
    public static void main(String[] args) {
        MappedByteBuffer shm = createSharedMemory("../shared.dat", 64 * 1024);

        for (int i = 0; i < shm.capacity() / Integer.BYTES; i++) {
            int value = shm.getInt(i * Integer.BYTES);
            if(value==0){
              break;
            }
            System.out.print(value + " ");
        }

    }

    private static MappedByteBuffer createSharedMemory(String path, int size) {
        try (FileChannel fileChannel = FileChannel.open(Paths.get(path), StandardOpenOption.READ, StandardOpenOption.WRITE, StandardOpenOption.CREATE)) {
            return fileChannel.map(FileChannel.MapMode.READ_WRITE, 0, size);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
