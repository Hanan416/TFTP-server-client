/**
 * 
 */
package bgu.spl171.net.impl.bidi;

import java.awt.FileDialog;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import bgu.spl171.net.api.bidi.BidiMessagingProtocol;
import bgu.spl171.net.api.bidi.Connections;
import bgu.spl171.net.impl.bidi.MessagingProtocol.ACK;
import bgu.spl171.net.impl.bidi.MessagingProtocol.BCAST;
import bgu.spl171.net.impl.bidi.MessagingProtocol.DATA;
import bgu.spl171.net.impl.bidi.MessagingProtocol.DELRQ;
import bgu.spl171.net.impl.bidi.MessagingProtocol.ERROR;
import bgu.spl171.net.impl.bidi.MessagingProtocol.LOGRQ;
import bgu.spl171.net.impl.bidi.MessagingProtocol.MessageForProtocol;
import bgu.spl171.net.impl.bidi.MessagingProtocol.RRQ;
import bgu.spl171.net.impl.bidi.MessagingProtocol.WRQ;

/**
 * @author user
 *
 */
public class BidiMessagingProtocolImpl<T> implements BidiMessagingProtocol<MessageForProtocol> {
	private boolean _shouldTerminate;
	private static ConcurrentHashMap<Integer, String> _allLoggedUsers = new ConcurrentHashMap<Integer,String>();
	private int connectionId;
	private Connections<MessageForProtocol> _allConnections;

	/*************DATA Manegmant******************/
	private InputStream _writeBuffer;
	private FileOutputStream _outBuffer;
	private ConcurrentLinkedQueue<byte[]> _queueingBytesToFile= new ConcurrentLinkedQueue<>();
	private int _numOfByteToWrite;
	private short _numOfPacket=0;
	private boolean finishedSend=false;

	private String fileDir = System.getProperty("user.dir") + File.separator + "Files";



	private boolean isLogged;
	private String _fileName;




	public void start(int connectionId, Connections<MessageForProtocol> connections) {

		_shouldTerminate=false;
		_numOfByteToWrite=0;
		_allConnections=connections;
		this.connectionId=connectionId;
		isLogged=false;
	}

	@Override
	public void process(MessageForProtocol message) {

		MessageForProtocol _replay=null;
		if(message.getOpCodeVal()!=7 && !isLogged)
			_replay=new ERROR((short)6);
		else{
			switch(message.getOpCodeVal()){
			case 1: {
				RRQ _temp = (RRQ)message;
				_fileName=_temp.get_fileName();
				try {
					finishedSend=false;
					loadFileFromPathToBuffer(fileDir + File.separator +_temp.get_fileName());
					_replay=creatDataPackets();
				} catch (Exception e) {
					_replay = new ERROR((short)1);
				}
				break;
			}
			case 2: {WRQ _temp = (WRQ)message;
			_fileName=_temp.get_fileName();
			if(doesFileExist(_temp.get_fileName())){
				_replay=new ERROR((short)5);
			}
			_replay=new ACK((short)0);
			break;
			}
			case 3: {DATA _temp = (DATA)message;
			if (_temp.get_packetSize()<512){
				_queueingBytesToFile.add(_temp.get_data());
				_numOfByteToWrite+=_temp.get_packetSize();
				_replay=new ACK(_temp.get_numOfBlocks());
				try {
					createFile(_fileName);
					_allConnections.broadcast(new BCAST(true,_fileName));
					_fileName=null;

				} catch (Exception e) {
					_replay=new ERROR((short)5);
				}finally {
					_numOfByteToWrite=0;
				}
			}
			else{
				_queueingBytesToFile.add(_temp.get_data());
				_numOfByteToWrite+=_temp.get_packetSize();
				_replay=new ACK(_temp.get_numOfBlocks());}
			break;
			}
			case 4: {ACK _temp = (ACK)message;
			try{
				if (_writeBuffer.available()>0){
					_replay=creatDataPackets();

				}
				else if (_writeBuffer.available()==0 && !finishedSend){
					_replay=creatDataPackets();

					finishedSend=true;
				}
			}catch(Exception e){

			}
			}break;
			case 5: {
				_writeBuffer=null;
				_outBuffer=null;
				break;
			}
			case 6: 
			{
				File _file = new File(fileDir);
				String[] _allFiles = _file.list();
				if(_allFiles.length == 0){
					_replay = new DATA((short)0,(short)1, null);
					break;
				}
				String _allFilesString = "";
				for (String _fileName:_allFiles)
				{
					_allFilesString=_allFilesString+_fileName+'\0';
				}
				_writeBuffer=new ByteArrayInputStream(_allFilesString.getBytes());
				_numOfPacket=0;
			}
			try {
				_replay=creatDataPackets();
			} catch (Exception e1) {
				_replay=new ERROR((short)1);
			}
			break;
			case 7: {
				LOGRQ _temp=(LOGRQ)message;
				if (_allLoggedUsers.containsKey(connectionId)){
					_replay=new ERROR((short)0);

				}
				else if (_allLoggedUsers.contains(_temp.get_UserName())){
					_replay=new ERROR((short)7);
				}
				else{
					_allLoggedUsers.put(connectionId, _temp.get_UserName());
					isLogged = true;
					_replay=new ACK((short)0);
				}
			}
			break;
			case 8: {
				DELRQ _temp=(DELRQ)message;
				File[] _allFiles=null;
				try{
					File folder = new File(fileDir);
					_allFiles = folder.listFiles();
					boolean found=doesFileExist(_temp.get_fileName());
					if (!found){ _replay=new ERROR((short)1);}
					File _file = new File(fileDir + File.separator + _temp.get_fileName());
					boolean flag;
					flag= _file.delete();
					_replay = new ACK((short)0); //TODO:: added 19:09
					_allConnections.broadcast(new BCAST(false,_temp.get_fileName()));
				}catch(Exception e){
					_replay=new ERROR((short)2);
				}
				break;
			}
			case 9: {_replay=new ERROR((short)4);
			break;
			}
			case 10: {_replay=new ACK((short) 0);
			_shouldTerminate=true;
			break;
			}
			default: _replay=new ERROR((short)4);
			}
		}
		_allConnections.send(connectionId, _replay);
	}

	private boolean doesFileExist(String fileName){
		File file;
		file = new File(fileDir + File.separator + fileName);
		return file.exists();

	}


	public void loadFileFromPathToBuffer(String fileName)throws Exception{
		File _file = new File(fileName);
		_writeBuffer=new FileInputStream(_file);

	}

	public void createFile(String fileName)throws Exception{
		File file;
		try {
			//Specify the file path here
			file = new File(fileDir + File.separator + fileName);
			_outBuffer = new FileOutputStream(file);

			/* This logic will check whether the file
			 * exists or not. If the file is not found
			 * at the specified location it would create
			 * a new file*/
			

			/*String content cannot be directly written into
			 * a file. It needs to be converted into bytes
			 */

			byte[] _data = new byte[_numOfByteToWrite];
			int _counter=0;
			while (!_queueingBytesToFile.isEmpty()){
				byte[] _temp=_queueingBytesToFile.poll();
				System.arraycopy(_temp, 0 , _data, _counter*512, _temp.length);
				_counter++;
			}

			_outBuffer.write(_data);
			_outBuffer.flush();
			_numOfByteToWrite=0;
		} 
		finally {
			try {
				if (_outBuffer != null) 
				{
					_outBuffer.close();
				}
			}catch(Exception e){

			}
		}
	}


	public DATA creatDataPackets() throws Exception{

		InputStream copyStream = _writeBuffer;
		int _dataPacketSize = copyStream.available();
		byte[] temp=new byte[512];
		if(_writeBuffer.available()>=512){
			_writeBuffer.read(temp, 0, 512);
			_numOfPacket++;
			return new DATA((short)512,_numOfPacket, temp);
		}
		else{
			_writeBuffer.read(temp, 0, _dataPacketSize);
			finishedSend=true;
			DATA _return=new DATA((short)_dataPacketSize, _numOfPacket, temp);
			_numOfPacket=0;
			return _return;

		}
	}


	@Override
	public boolean shouldTerminate() {
		return _shouldTerminate;
	}



}
