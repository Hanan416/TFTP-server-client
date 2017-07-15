package bgu.spl171.net.impl.bidi.MessagingProtocol;

public class LOGRQ extends MessageForProtocol {
	private String _UserName;
	
	
	public LOGRQ(String userName){
		this._UserName=userName;
	}
	////////////////////////////////////////////
	public String get_UserName() {
		return _UserName;
	}

	protected void set_UserName(String _UserName) {
		this._UserName = _UserName;
	}
	
	
	
	/////////////////////////////////////////////////
	@Override
	public short getOpCodeVal() {
		return 7;
	}
	

	
	/////////////////////////////////////////////////
	public String toString(){
		return getOpCodeVal()+_UserName+"0";
	}
	
}
