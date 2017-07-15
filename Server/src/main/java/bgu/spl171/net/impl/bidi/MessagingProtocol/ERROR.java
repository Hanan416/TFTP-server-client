package bgu.spl171.net.impl.bidi.MessagingProtocol;

public class ERROR extends MessageForProtocol {
	private short _errorCode;
	private String _errorMessage;
	
	
	private final String[] ErrorMessage=new String[]{"Not defined",
										"File not found",
										"Access violation",
										"Disk full or allocation exceeded",
										"Illegal TFTP operation",
										"File already exists",
										"User not logged in.",
										"User already logged in"};

	
	public ERROR(short _errorCode) {
		super();
		this._errorCode = _errorCode;
		this._errorMessage = ErrorMessage[_errorCode];
	}


	////////////////////////////////////////////////////////
	public short get_errorCode() {
		return _errorCode;
	}


	protected void set_errorCode(short _errorCode) {
		this._errorCode = _errorCode;
		_errorMessage=ErrorMessage[_errorCode];
	}


	protected String get_errorMessage() {
		return _errorMessage;
	}


	protected void set_errorMessage(String _errorMessage) {
		this._errorMessage = _errorMessage;
	}

	///////////////////////////////////////////////////////
	
	@Override
	public short getOpCodeVal() {
		return 5;
	}

	////////////////////////////////////////////////

	@Override
	public String toString() {
		return getOpCodeVal()+""+get_errorCode()+get_errorMessage();
	}
	
	
	

}
