package dk.easv.easvexam.be;

public class MyException extends RuntimeException {
    public MyException(String message, Throwable cause) {
        super(message,  cause);
    }
}
