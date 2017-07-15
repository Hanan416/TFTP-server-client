#include "connectionHandler.h"
#include <boost/asio.hpp>
#include <boost/thread.hpp>
#include <stdio.h>
#include <boost/algorithm/string.hpp>
using namespace std;

connectionHandler::connectionHandler(string host, short port): host_(host), port_(port), io_service_(), socket_(io_service_),_Filestream(),_fileNameInTransaction(){}

connectionHandler::~connectionHandler() {
	close();
}

bool connectionHandler::connect() {
	try {
		tcp::endpoint endpoint(boost::asio::ip::address::from_string(host_), port_); // the server endpoint
		boost::system::error_code error;
		socket_.connect(endpoint, error);
		if (error)
			throw boost::system::system_error(error);
	}
	catch (std::exception& e) {
		std::cerr << "Connection failed (Error: " << e.what() << ')' << std::endl;
		return false;
	}
	return true;
}


//create UTF-8 corrospoinding
bool connectionHandler::getBytes(char bytes[], unsigned int bytesToRead) {
	size_t tmp = 0;
	boost::system::error_code error;
	try {
		while (!error && bytesToRead > tmp ) {
			tmp += socket_.read_some(boost::asio::buffer(bytes+tmp, bytesToRead-tmp), error);
		}
		if(error)
			throw boost::system::system_error(error);
	} catch (std::exception& e) {
		std::cerr << "recv failed (Error: " << e.what() << ')' << std::endl;
		return false;
	}
	return true;
}

bool connectionHandler::sendBytes(const char bytes[], int bytesToWrite) {
	int tmp = 0;
	boost::system::error_code error;
	try {
		while (!error && bytesToWrite > tmp ) {
			tmp += socket_.write_some(boost::asio::buffer(bytes + tmp, bytesToWrite - tmp), error);
		}
		if(error)
			throw boost::system::system_error(error);
	} catch (std::exception& e) {
		std::cerr << "recv failed (Error: " << e.what() << ')' << std::endl;
		return false;
	}
	return true;
}

bool connectionHandler::getLine(std::string& line) {
	return getFrameAscii(line, '\0');
}

bool connectionHandler::sendLine(std::string& line) {
	return sendFrameAscii(line, '\0');
}

bool connectionHandler::getFrameAscii(std::string& frame, char delimiter) {
	char ch;
	// Stop when we encounter the null character.
	// Notice that the null character is not appended to the frame string.
	try {
		do{
			getBytes(&ch, 1);
			frame.append(1, ch);
		}while (delimiter != ch);
	} catch (std::exception& e) {
		std::cerr << "recv failed (Error: " << e.what() << ')' << std::endl;
		return false;
	}
	return true;
}

bool connectionHandler::sendFrameAscii(const std::string& frame, char delimiter) {
	bool result=sendBytes(frame.c_str(),frame.length());
	if(!result) return false;
	return sendBytes(&delimiter,1);
}

// Close down the connection properly.
void connectionHandler::close() {
	try{
		socket_.close();
		if (_Filestream.is_open())
			_Filestream.close();
	} catch (...) {
	}
}
bool connectionHandler::isClosed(){
	return !socket_.is_open();
}

bool connectionHandler::encodeMessage(string& _lineCommand){
	vector<char> _ans;
	bool _shouldTerminate=true;
	vector<string> _brokenString;
	boost::split(_brokenString,_lineCommand,boost::is_space());
	if (_brokenString[0].compare("RRQ")==0 &&_brokenString.size()==2){
		_ans.push_back(0);
		_ans.push_back(1);
		_fileNameInTransaction=_brokenString[1];
		unsigned int i=0;
		while(i<_brokenString[1].size()){
			_ans.push_back(_brokenString[1].at(i));
			i++;
		}
		_ans.push_back('\0');
		char _stringToChar[_brokenString[1].length()+1];
		std::strcpy(_stringToChar,_brokenString[1].c_str());
		_Filestream.open(_stringToChar,std::fstream::out);
		_isRead=true;
	}
	else if (_brokenString[0].compare("WRQ")==0 &&_brokenString.size()==2){
		_ans.push_back(0);
		_ans.push_back(2);
		_fileNameInTransaction=_brokenString[1];
		unsigned int i=0;
		while(i<_brokenString[1].size()){
			_ans.push_back(_brokenString[1].at(i));
			i++;
		}
		_ans.push_back('\0');
		char _stringToChar[_brokenString[1].length()+1];
		std::strcpy(_stringToChar,_brokenString[1].c_str());
		_Filestream.open(_stringToChar,std::ios::in);
		if (!_Filestream.good())
		{
			_Filestream.close();
			cout<<"ERROR 1"<<endl;
			return false;
		}
	}
	else if (_brokenString[0].compare("DIRQ")==0 &&_brokenString.size()==1){
		_ans.push_back(0);
		_ans.push_back(6);
	}
	else if (_brokenString[0].compare("LOGRQ")==0 && _brokenString.size()==2){
		_ans.push_back(0);
		_ans.push_back(7);
		unsigned int i=0;
		while(i<_brokenString[1].size()){
			_ans.push_back(_brokenString[1].at(i));
			i++;
		}
		_ans.push_back('\0');
	}
	else if (_brokenString[0].compare("DELRQ")==0&&_brokenString.size()==2){
		_ans.push_back(0);
		_ans.push_back(8);
		unsigned int i=0;
		while(i<_brokenString[1].size()){
			_ans.push_back(_brokenString[1].at(i));
			i++;
		}
		_ans.push_back('\0');
	}
	else if (_brokenString[0].compare("DISC")==0&&_brokenString.size()==1){
		_ans.push_back(0);
		_ans.push_back(10);
		this->_sentDisc=true;
	}
	else{
		cout<<"Error 4"<<endl;
		_shouldTerminate=false;
	}
	bool ans=false;
	if (_ans.size()>=2){
		char _message[_ans.size()];
		int _counter=0;
		for (char _tempChar:_ans){
			_message[_counter]=_tempChar;
			_counter++;
		}


		ans=sendBytes(_message,_ans.size());
	}
	if (_shouldTerminate&&!ans)
		return false;
	return true;
}

void connectionHandler::decode(){
	char _opcode[2];
	vector<char> _ans;
	getBytes(_opcode,2);
	short _opVal=bytesToShort(_opcode);
	if (_opVal==3){  //DATA
		char _packetSize[2];
		getBytes(_packetSize,2);
		short _lengthOfPacket=bytesToShort(_packetSize);
		char _packetNum[2];
		getBytes(_packetNum,2);
		short _temp=bytesToShort(_packetNum);
		_ans.push_back(0);
		_ans.push_back(4);
		char _AckOn[2];
		shortToBytes(_temp,_AckOn);
		_ans.push_back(_AckOn[0]);
		_ans.push_back(_AckOn[1]);

		char _message[_ans.size()];
		int _counter=0;
		for (char _tempChar:_ans){
			_message[_counter]=_tempChar;
			_counter++;
		}
		sendBytes(_message,4);
		char _data[_lengthOfPacket];
		getBytes(_data,_lengthOfPacket);
		if (_Filestream.is_open()){
			_Filestream.write(_data,_lengthOfPacket);
			_Filestream.flush();
			if (_lengthOfPacket<512){
				_isRead=false;
				_Filestream.close();
				cout<<"RRQ " +_fileNameInTransaction + " complete"<<endl;
				_fileNameInTransaction="";
			}
		}
		else{
			int i=0;
			while (i<_lengthOfPacket){
				if(_data[i]!='\0')
					cout<<_data[i];
				else
					cout<<endl;
				i++;
			}
		}


	}
	else if (_opVal==4){  //ACK

		char _ackNumber[2];
		getBytes(_ackNumber,2);
		short _ackNum=bytesToShort(_ackNumber);
		cout<<"ACK "<<_ackNum<<endl;
		if (this->_sentDisc){
					close();
					return;
				}
		if (_Filestream.is_open()){

			//geting the current data block num
			char _packetNum[2];
			short _numOfPacket=_ackNum+1;
			shortToBytes(_numOfPacket,_packetNum);


			// getting the current data
			vector<char> _dataTemp;
			int i=0;
			char _temp;
			while (i<512 /*&& !_Filestream.fail()*/ && _Filestream.peek()!= EOF){
				_Filestream.get(_temp);
				_dataTemp.push_back(_temp);
				i++;
			}

			//getting the data size
			char  _sizeOfNewPacket[2];
			shortToBytes(_dataTemp.size(),_sizeOfNewPacket);
			// checking if it is the last packet to send
			if (_dataTemp.size()<512){
				_Filestream.close();
				cout<<"WRQ "+ _fileNameInTransaction +" complete"<<endl;
			}
			//copying all data to char[] in order to send
			char _message[_dataTemp.size()+6];
			_message[0]= 0;
			_message[1]= 3;
			_message[2]= _sizeOfNewPacket[0];
			_message[3]= _sizeOfNewPacket[1];
			_message[4]= _packetNum[0];
			_message[5]= _packetNum[1];
			int _counter=6;


			for(char _tempChar:_dataTemp){
				_message[_counter]=_tempChar;
				_counter++;
			}
			sendBytes(_message,_dataTemp.size()+6);

		}
	}
	else if (_opVal==5){   // ERROR
		char _numOfError[2];
		string _temp;
		getBytes(_numOfError,2);
		short _numErrorShort = bytesToShort(_numOfError);
		cout<<"Error "<<_numErrorShort<<endl;
		getFrameAscii(_temp,'\0');
		if (_Filestream.is_open()){
			_Filestream.close();
			char _stringToChar[_fileNameInTransaction.length()+1];
			std::strcpy(_stringToChar,_fileNameInTransaction.c_str());
			if (_isRead)
				remove(_stringToChar);

		}
		_sentDisc=false;
		_fileNameInTransaction="";
	}
	else if (_opVal==9){    // BCAST
		char _isAdded[1];
		string _status;
		getBytes(_isAdded,1);
		short _boolValue=bytesToShort(_isAdded);
		if (_boolValue==1)
			_status=" add ";
		else
			_status=" del ";
		string _fileName;
		getFrameAscii(_fileName,'\0');
		cout<<"BCAST"<<_status<<_fileName<<endl;

	}

}

short connectionHandler::bytesToShort(char* bytesArr)
{
	short result = (short)((bytesArr[0] & 0xff) << 8);
	result += (short)(bytesArr[1] & 0xff);
	return result;
}

void connectionHandler::shortToBytes(short num, char* bytesArr)
{
	bytesArr[0] = ((num >> 8) & 0xFF);
	bytesArr[1] = (num & 0xFF);
}
