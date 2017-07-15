package bgu.spl171.net.impl.bidi.MessagingProtocol;

public class WRQ extends MessageForProtocol {
	private String _fileName;
	
	
	public WRQ(String fileName){
		super();
		this._fileName=fileName;
	}

	///////////////////////////////////////////

	public String get_fileName() {
		return _fileName;
	}


	protected void set_fileName(String _fileName) {
		this._fileName = _fileName;
	}
	
	///////////////////////////////////////////
	
	
	@Override
	public short getOpCodeVal() {
		return 2;
	}


	//////////////////////////////////////////
	
	
	@Override
	public String toString() {
		return getOpCodeVal()+_fileName+0;
	}
}
	
	


