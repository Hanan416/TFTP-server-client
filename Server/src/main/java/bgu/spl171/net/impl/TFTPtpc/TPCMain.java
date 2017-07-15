package bgu.spl171.net.impl.TFTPtpc;

import java.util.function.Supplier;

import bgu.spl171.net.api.MessageEncoderDecoder;
import bgu.spl171.net.api.bidi.BidiMessagingProtocol;
import bgu.spl171.net.impl.bidi.BidiMessagingProtocolEncoderDecoder;
import bgu.spl171.net.impl.bidi.BidiMessagingProtocolImpl;
import bgu.spl171.net.impl.bidi.MessagingProtocol.MessageForProtocol;
import bgu.spl171.net.srv.Server;

public class TPCMain {

	public static void main(String[] args) {
		int port = Integer.parseInt(args[0]);
		Supplier<BidiMessagingProtocol<MessageForProtocol>> protocolSupplier = BidiMessagingProtocolImpl::new;
		Supplier<MessageEncoderDecoder<MessageForProtocol>> bidiEncoder = BidiMessagingProtocolEncoderDecoder::new;
		Server.threadPerClient(port, protocolSupplier, bidiEncoder).serve();
	}

}
