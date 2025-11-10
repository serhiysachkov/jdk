import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;

/**
 * Example of serializer approach to generate .ser  files for new classes, should be updated to use as cmd tool
 */
public class Serializer {

    public static void main(String[] args) throws IOException {
        System.out.println("java version: " + Runtime.version());
        String className = "JavaTimeSerializer";
        try{
            Class c = Class.forName(className);
            /*heck if Serialazable should be added here or logic to
             create new instance could be improved to check during getting Class*/
            FileOutputStream fileOutputStream = new FileOutputStream(className + "-" + Runtime.version() + ".ser");
            ObjectOutputStream oos = new ObjectOutputStream(fileOutputStream);
            oos.writeObject(c.getDeclaredConstructor().newInstance());
            oos.close();
        } catch (ClassNotFoundException | InvocationTargetException
                 | InstantiationException | IllegalAccessException |
                 NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }
}
