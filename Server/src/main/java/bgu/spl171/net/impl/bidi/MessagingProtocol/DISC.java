package bgu.spl171.net.impl.bidi.MessagingProtocol;

public class DISC extends MessageForProtocol {

	public DISC() {
		super();
	}

	/////////////////////////////////////
	@Override
	public short getOpCodeVal() {
		return 10;
	}
	///////////////////////////////////////
	
	@Override
	public String toString() {
		return ""+getOpCodeVal();
	}

}
