CFLAGS:=-c -Wall -Weffc++ -g -std=c++11 -Iinclude
LDFLAGS:=-lboost_system -lboost_locale -lboost_thread
OFILES:=bin/connectionHandler.o bin/TFTPClient.o

all: TFTPClient
	g++ -o bin/TFTPClient $(OFILES) $(LDFLAGS)

TFTPClient: $(OFILES)
	
bin/TFTPClient.o: src/TFTPClient.cpp
	g++ $(CFLAGS) -o bin/TFTPClient.o src/TFTPClient.cpp
	
bin/connectionHandler.o: src/connectionHandler.cpp
	g++ $(CFLAGS) -o bin/connectionHandler.o src/connectionHandler.cpp
	

.PHONY: clean
clean:
	rm -f bin/*