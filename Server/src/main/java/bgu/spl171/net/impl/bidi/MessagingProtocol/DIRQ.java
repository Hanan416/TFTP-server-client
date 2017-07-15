package bgu.spl171.net.impl.bidi.MessagingProtocol;

public class DIRQ extends MessageForProtocol {

	@Override
	public short getOpCodeVal() {
		return 6;
	}

	public DIRQ() {
		super();
	}

	@Override
	public String toString() {
		return ""+getOpCodeVal();
	}

	
}
