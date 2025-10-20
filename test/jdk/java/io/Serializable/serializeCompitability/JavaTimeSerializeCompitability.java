
import java.io.IOException;

import java.io.InputStream;
import java.io.ObjectInputStream;


/* @test
 *
 * @bug 8334742
 *
 * @summary Test
 */

public class JavaTimeSerializeCompitability {

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        JavaTimeSerializeCompitability test = new JavaTimeSerializeCompitability();
        test.deserializeJDK17Time();
        test.deserializeJDK21Time();
    }

    private void deserializeJDK17Time(){
        try(InputStream in = this.getClass().getResourceAsStream("javaTimeSerializer_17.ser");
        //FileInputStream fis = new FileInputStream("javaTimeSerializer_17.ser");
        ObjectInputStream ois = new ObjectInputStream(in);) {
            JavaTimeSerializer javaTimeSerializer = (JavaTimeSerializer)ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private void deserializeJDK21Time(){
        try(InputStream in = this.getClass().getResourceAsStream("javaTimeSerializer_21.ser");
            //FileInputStream fis = new FileInputStream("javaTimeSerializer_17.ser");
            ObjectInputStream ois = new ObjectInputStream(in);) {
            JavaTimeSerializer javaTimeSerializer = (JavaTimeSerializer)ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}