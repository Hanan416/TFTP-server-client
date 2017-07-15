package bgu.spl171.net.impl.bidi.MessagingProtocol;

public class ACK extends MessageForProtocol {
	private short _blockNum;
	
	
	public ACK(short _blockNum) {
		super();
		this._blockNum = _blockNum;
	}

	///////////////////////////////////////////////

	public short get_blockNum() {
		return _blockNum;
	}

	protected void set_blockNum(short _blockNum) {
		this._blockNum = _blockNum;
	}
	
	
	///////////////////////////////////////////////
	@Override
	public short getOpCodeVal() {
		return 4;
	}
	
	//////////////////////////////////////////////
	@Override
	public String toString() {
		return ""+getOpCodeVal()+_blockNum;
	}


}
