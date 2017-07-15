#ifndef CONNECTION_HANDLER__

#define CONNECTION_HANDLER__

#include <fstream>
#include <vector>
#include <string>
#include <iostream>
#include <boost/asio.hpp>

using namespace std;


using boost::asio::ip::tcp;




class connectionHandler {

private:

	const std::string host_;

	const short port_;

	boost::asio::io_service io_service_;   // Provides core I/O functionality

	tcp::socket socket_;

	bool _isRead=false;

    short bytesToShort(char* bytesArr);

    void shortToBytes(short num, char* bytesArr);

public:
    bool _sentDisc=false;

	fstream _Filestream;

	string _fileNameInTransaction;

	connectionHandler(std::string host, short port);

	virtual ~connectionHandler();

 	// Connect to the remote machine

	bool connect();

	bool isClosed();

	// Read a fixed number of bytes from the server - blocking.

	// Returns false in case the connection is closed before bytesToRead bytes can be read.

	bool getBytes(char bytes[], unsigned int bytesToRead);


	// Send a fixed number of bytes from the client - blocking.

	// Returns false in case the connection is closed before all the data is sent.

	bool sendBytes(const char bytes[], int bytesToWrite);


// Read an ascii line from the server

	// Returns false in case connection closed before a newline can be read.

	bool getLine(std::string& line);


// Send an ascii line from the server
	// Returns false in case connection closed before all the data is sent.

	bool sendLine(std::string& line);

 	// Get Ascii data from the server until the delimiter character

	// Returns false in case connection closed before null can be read.

	bool getFrameAscii(std::string& frame, char delimiter);



	// Send a message to the remote host.

	// Returns false in case connection is closed before all the data is sent.

	bool sendFrameAscii(const std::string& frame, char delimiter);

	void close();
    // Close down the connection properly.


    void decode();

    bool encodeMessage(string& _lineCommand);

    char vecToChar(vector<char> toCopy);

    vector<char> CharToVec(char toCopy[]);
};

 //class ConnectionHandler


#endif
