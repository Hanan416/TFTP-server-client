package bgu.spl171.net.impl.bidi.MessagingProtocol;

public class DATA extends MessageForProtocol {
	private short _packetSize;
	private short _numOfBlocks;
	private byte[] _data;
	
	
	public DATA(short _packetSize, short _numOfBlocks, byte[] _data) {
		super();
		this._packetSize = _packetSize;
		this._numOfBlocks = _numOfBlocks;
		this._data = _data;
	}
	//////////////////////////////////////
	
	public short get_packetSize() {
		return _packetSize;
	}



	protected void set_packetSize(short _packetSize) {
		this._packetSize = _packetSize;
	}



	public short get_numOfBlocks() {
		return _numOfBlocks;
	}



	protected void set_numOfBlocks(short _numOfBlocks) {
		this._numOfBlocks = _numOfBlocks;
	}



	public byte[] get_data() {
		return _data;
	}



	protected void set_data(byte[] _data) {
		this._data = _data;
	}
	////////////////////////////////////
	

	@Override
	public short getOpCodeVal() {
		return 3;
	}

	@Override
	public String toString() {
		return getOpCodeVal()+""+get_packetSize()+""+get_numOfBlocks()+get_data();
	}
	
	
}
