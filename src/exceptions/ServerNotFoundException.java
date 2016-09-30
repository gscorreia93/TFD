package exceptions;

public class ServerNotFoundException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public ServerNotFoundException() {
		super("There are no more available servers to connect");
	}

	public ServerNotFoundException(String arg0) {
		super(arg0);
	}
}
