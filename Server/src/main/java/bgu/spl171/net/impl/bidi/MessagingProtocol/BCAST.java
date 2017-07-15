package bgu.spl171.net.impl.bidi.MessagingProtocol;

public class BCAST extends MessageForProtocol {
	private boolean wasAdded;
	private String _fileName;
	
	
	public BCAST(boolean wasAdded, String fileName) {
		super();
		this.wasAdded = wasAdded;
		this._fileName=fileName;
	}
	
	/////////////////////////////////////////////
	protected void setWasAdded(boolean wasAdded) {
		this.wasAdded = wasAdded;
	}

	public boolean getWasAdded() {
		return wasAdded;
	}

	public String get_fileName() {
		return _fileName;
	}

	protected void set_fileName(String _fileName) {
		this._fileName = _fileName;
	}


	////////////////////////////////////////////////
	@Override
	public short getOpCodeVal() {
		return 9;
	}

	
	////////////////////////////////////////////////
	@Override
	public String toString() {
		return getOpCodeVal()+""+wasAdded+_fileName;
	}

}
