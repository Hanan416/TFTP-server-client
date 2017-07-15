#include "../include/connectionHandler.h"

#include <boost/thread.hpp>
#include <boost/asio/io_service.hpp>


void reader(connectionHandler *connectionHandler) {
	while (1) {
		if (connectionHandler->isClosed()) {
			break;
		}
		connectionHandler->decode();
		}

	}


int main(int argc, char *argv[]) {
	if (argc < 3) {
		std::cerr << "Usage: " << argv[0] << " host port" << std::endl
				<< std::endl;
		return -1;
	}
	std::string host = argv[1];
	short port = atoi(argv[2]);

	connectionHandler connectionHandler(host, port);
	if (!connectionHandler.connect()) {
		std::cerr << "Cannot connect to " << host << ":" << port << std::endl;
		return 1;
	}

	boost::thread readerThread(reader, &connectionHandler);
	//From here we will see the rest of the ehco client implementation:
	while (!connectionHandler._sentDisc) {
		const short bufsize = 1024;
		char buf[bufsize];
		std::cin.getline(buf, bufsize); //getting the line of the user command
		std::string line(buf); //convert it to string



		if (connectionHandler.isClosed() || !connectionHandler.encodeMessage(line)) {
			break;
		}

	}
	readerThread.join();
	return 0;
}

