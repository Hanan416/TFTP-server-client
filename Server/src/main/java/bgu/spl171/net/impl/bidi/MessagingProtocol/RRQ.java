package bgu.spl171.net.impl.bidi.MessagingProtocol;

public class RRQ extends MessageForProtocol {
	private String _fileName;
	
	
	public RRQ(String fileName){
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
		return 1;
	}


	//////////////////////////////////////////
	
	
	@Override
	public String toString() {
		return getOpCodeVal()+_fileName+0;
	}
	
	


}
