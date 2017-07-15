package bgu.spl171.net.impl.TFTPreactor;

import java.util.function.Supplier;
import bgu.spl171.net.api.MessageEncoderDecoder;
import bgu.spl171.net.api.bidi.BidiMessagingProtocol;
import bgu.spl171.net.impl.bidi.BidiMessagingProtocolEncoderDecoder;
import bgu.spl171.net.impl.bidi.BidiMessagingProtocolImpl;
import bgu.spl171.net.impl.bidi.MessagingProtocol.MessageForProtocol;
import bgu.spl171.net.srv.Server;

public class ReactorMain {

	public static void main(String[] args) {
		int port = Integer.parseInt(args[0]);
		Supplier<BidiMessagingProtocol<MessageForProtocol>> protocolSupplier = BidiMessagingProtocolImpl::new;
		Supplier<MessageEncoderDecoder<MessageForProtocol>> bidiEncoder = BidiMessagingProtocolEncoderDecoder::new;
		Server.reactor(Runtime.getRuntime().availableProcessors(),port,protocolSupplier,bidiEncoder).serve();
	}

}
