public class CheckJavaFX {
    public static void main(String[] args) {
        try {
            Class.forName("javafx.scene.media.Media");
            System.out.println("JavaFX Available");
        } catch (ClassNotFoundException e) {
            System.out.println("JavaFX NOT Available");
        }
    }
}
