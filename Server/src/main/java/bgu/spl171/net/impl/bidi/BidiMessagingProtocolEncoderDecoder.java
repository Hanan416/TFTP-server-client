package bgu.spl171.net.impl.bidi;


import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import bgu.spl171.net.api.MessageEncoderDecoder;
import bgu.spl171.net.impl.bidi.MessagingProtocol.ACK;
import bgu.spl171.net.impl.bidi.MessagingProtocol.BCAST;
import bgu.spl171.net.impl.bidi.MessagingProtocol.DATA;
import bgu.spl171.net.impl.bidi.MessagingProtocol.DELRQ;
import bgu.spl171.net.impl.bidi.MessagingProtocol.DIRQ;
import bgu.spl171.net.impl.bidi.MessagingProtocol.DISC;
import bgu.spl171.net.impl.bidi.MessagingProtocol.ERROR;
import bgu.spl171.net.impl.bidi.MessagingProtocol.LOGRQ;
import bgu.spl171.net.impl.bidi.MessagingProtocol.MessageForProtocol;
import bgu.spl171.net.impl.bidi.MessagingProtocol.RRQ;
import bgu.spl171.net.impl.bidi.MessagingProtocol.WRQ;

@SuppressWarnings("hiding")
public class BidiMessagingProtocolEncoderDecoder<T> implements MessageEncoderDecoder<MessageForProtocol>{

	private short _opCodeIndex=0;
	private byte[] _opCodeArr = new byte[2];
	private int _opPos = 0;
	private byte[] _commandArr = new byte[1<<10];
	private int _objectBytesIndex=0;

	private byte[] _number = new byte[2];
	private short _numberPos=0;

	private byte[] _blockNum = new byte[2];
	private short _blockIndex = 0;




	private void resetAll(){
		_opCodeIndex=0;
		_opCodeArr = new byte[2];
		_opPos = 0;
		_commandArr = new byte[1<<10];
		_objectBytesIndex=0;

		_number = new byte[2];
		_numberPos=0;

		_blockNum = new byte[2];
		_blockIndex = 0;
	}


	public MessageForProtocol decodeNextByte(byte nextByte) {
		if(_opCodeIndex == 0){
			_opCodeArr[_opPos] = nextByte;

			//Creating the opCode short Val
			if(_opPos == 1){
				_opCodeIndex = bytesToShort(_opCodeArr);
			}
			if(_opCodeIndex == (short)6){

				resetAll();
				return new DIRQ();

			}
			if(_opCodeIndex == (short)10){

				resetAll();
				return new DISC();
			}
			_opPos++;

			return null;
		}
		else{
			return creatCommand(nextByte);
		}
	}

	public MessageForProtocol creatCommand(byte nextByte){
		if(_opCodeIndex == (short)1 || _opCodeIndex ==(short)2 || _opCodeIndex ==(short)7 || _opCodeIndex == (short)8){
			if(nextByte != '\0'){
				pushByte(nextByte);
				return null;
			}
			else{
				String _name = popString(0, _objectBytesIndex);
				switch (_opCodeIndex){
				case ((short)1):{
					resetAll();
					return new RRQ(_name);
				}

				case ((short)2):{
					resetAll();
					return new WRQ(_name);
				}

				case ((short)7):{
					resetAll();

					return new LOGRQ(_name);
				}

				case ((short)8):{
					resetAll();
					return new DELRQ(_name);
				}
				}
				resetAll();
				return new ERROR((short)0);
			}
		}


		if(_opCodeIndex == (short)4 || _opCodeIndex == (short)5 || _opCodeIndex == (short)3){
			if(_numberPos < 2){
				_number[_numberPos] = nextByte;
				_numberPos++;
				if (_numberPos==2){
					if(_opCodeIndex == (short)4){
						resetAll();
						return new ACK(bytesToShort(_number));
					}
				}
				return null;
			}
			else
				return creatMessage(nextByte);
		}
		else{
			resetAll();
			return new ERROR((short)4);
		}
	}


	public MessageForProtocol creatMessage(Byte nextByte){
		if(_opCodeIndex == (short)4 || _opCodeIndex== (short)5){
			if(nextByte == '\0'){
				short num = bytesToShort(_number);
				if(_opCodeIndex == (short)4){
					resetAll();
					return new ACK(num);
				}
				else{
					resetAll();
					return new ERROR(num);
				}
			}
			else{
				resetAll();
				return new ERROR((short)0);
			}
		}
		else{
			if(_blockIndex < 2){
				_blockNum[_blockIndex] = nextByte;
				_blockIndex++;
				return null;
			}
			else
				return creatDataPacket(nextByte);

		}

	}

	private MessageForProtocol creatDataPacket(byte nextByte){
		if(_objectBytesIndex < bytesToShort(_number)){
			pushByte(nextByte);
		}
		if(_objectBytesIndex == bytesToShort(_number)){
			byte[] _rawData = new byte[_objectBytesIndex];
			for (int i=0;i<_objectBytesIndex;i++){
				_rawData[i]=_commandArr[i];
			}
			short _packetSize = bytesToShort(_number);
			short _blockNumer = bytesToShort(_blockNum);
			resetAll();
			return new DATA(_packetSize, _blockNumer,_rawData);

		}
		return null;
	}





	private void pushByte(byte nextByte) {
		if (_objectBytesIndex >= _commandArr.length) {
			_commandArr = Arrays.copyOf(_commandArr, _objectBytesIndex * 2);
		}

		_commandArr[_objectBytesIndex++] = nextByte;
	}




	/********************Short-byte[] manipulation**********************************/
	public short bytesToShort(byte[] byteArr)
	{
		short result = (short)((byteArr[0] & 0xff) << 8);
		result += (short)(byteArr[1] & 0xff);
		return result;
	}

	public byte[] shortToBytes(short num)
	{
		byte[] bytesArr = new byte[2];
		bytesArr[0] = (byte)((num >> 8) & 0xFF);
		bytesArr[1] = (byte)(num & 0xFF);
		return bytesArr;
	}

	@Override
	public byte[] encode(MessageForProtocol message) {
		short _opCodeKind = message.getOpCodeVal();
		byte[] _toReturn;
		byte[] _opCodeByte = shortToBytes(_opCodeKind);
		switch (_opCodeKind){
		case(1):{
			RRQ _Message=(RRQ)message;
			_toReturn=new byte[_Message.get_fileName().getBytes().length+3];
			System.arraycopy(_opCodeByte, 0, _toReturn, 0, 2);
			System.arraycopy(_Message.get_fileName().getBytes(), 0, _toReturn, 2, _Message.get_fileName().getBytes().length);
			_toReturn[_Message.get_fileName().getBytes().length+2]= '\0';  
			return _toReturn;

		}
		case(2):{
			WRQ _Message=(WRQ)message;
			_toReturn=new byte[_Message.get_fileName().getBytes().length+3];
			System.arraycopy(_opCodeByte, 0, _toReturn, 0, 2);
			System.arraycopy(_Message.get_fileName().getBytes(), 0, _toReturn, 2, _Message.get_fileName().getBytes().length);
			_toReturn[_Message.get_fileName().getBytes().length+2]= '\0';  
			return _toReturn;

		}
		case(3):{
			DATA _Message = (DATA)message;
			_toReturn=new byte[_Message.get_data().length+6];
			System.arraycopy(_opCodeByte, 0, _toReturn, 0, 2);
			System.arraycopy(shortToBytes(_Message.get_packetSize()), 0, _toReturn, 2, 2);
			System.arraycopy(shortToBytes(_Message.get_numOfBlocks()), 0, _toReturn, 4, 2);
			System.arraycopy(_Message.get_data(), 0, _toReturn, 6, _Message.get_data().length);
			return _toReturn;
		}
		case(4):{
			ACK _Message = (ACK)message;
			_toReturn=new byte[4];
			System.arraycopy(_opCodeByte, 0, _toReturn, 0, 2);
			System.arraycopy(shortToBytes(_Message.get_blockNum()), 0, _toReturn, 2, 2);
			return _toReturn;
		}
		case(5):{
			ERROR _Message = (ERROR)message;
			_toReturn=new byte[5];
			System.arraycopy(_opCodeByte, 0, _toReturn, 0, 2);
			System.arraycopy(shortToBytes(_Message.get_errorCode()), 0, _toReturn, 2, 2);
			_toReturn[4]='\0';
			return _toReturn;
		}
		case(6):{ //DIRQ
			_toReturn=new byte[2];
			System.arraycopy(_opCodeByte, 0, _toReturn, 0, 2);
			return _toReturn;
		}
		case(7):{
			LOGRQ _Message =(LOGRQ)message;
			_toReturn=new byte[_Message.get_UserName().getBytes().length+3];
			System.arraycopy(_opCodeByte, 0, _toReturn, 0, 2);
			System.arraycopy(_Message.get_UserName().getBytes(), 0, _toReturn, 2, _Message.get_UserName().getBytes().length);
			_toReturn[_Message.get_UserName().getBytes().length+2]='\0';
			return _toReturn;

		}
		case(8):{
			DELRQ _Message = (DELRQ)message;
			_toReturn=new byte[_Message.get_fileName().getBytes().length+3];
			System.arraycopy(_opCodeByte, 0, _toReturn, 0, 2);
			System.arraycopy(_Message.get_fileName().getBytes(), 0, _toReturn, 2, _Message.get_fileName().getBytes().length);
			_toReturn[_Message.get_fileName().getBytes().length+2]='\0';
			return _toReturn;

		}
		case(9):{
			BCAST _Message = (BCAST)message;
			_toReturn=new byte[_Message.get_fileName().getBytes().length+4];
			System.arraycopy(_opCodeByte, 0, _toReturn, 0, 2);
			_toReturn[2]=(byte)(_Message.getWasAdded()?1:0);
			System.arraycopy(_Message.get_fileName().getBytes(), 0, _toReturn, 3, _Message.get_fileName().getBytes().length);
			_toReturn[_Message.get_fileName().getBytes().length+3]='\0';
			return _toReturn;
		}
		case(10):{
			_toReturn=new byte[2];
			System.arraycopy(_opCodeByte, 0, _toReturn, 0, 2);
			return _toReturn;
		}
		default: return null;
		}

	}



	private String popString(int offset,int objectBytesIndex) {
		//notice that we explicitly requesting that the string will be decoded from UTF-8
		//this is not actually required as it is the default encoding in java.
		String result = new String(_commandArr, offset, objectBytesIndex, StandardCharsets.UTF_8);
		objectBytesIndex = 0;
		return result;
	}













}