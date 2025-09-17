public class JavaThreadSelfWrapper {

    public static void main(String[] args) {
        Thread thread = new Thread(new Thread());
        thread.start();
    }

}
