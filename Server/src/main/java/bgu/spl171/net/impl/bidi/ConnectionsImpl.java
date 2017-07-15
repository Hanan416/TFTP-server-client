package bgu.spl171.net.impl.bidi;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import bgu.spl171.net.api.bidi.Connections;
import bgu.spl171.net.srv.ConnectionHandler;

public class ConnectionsImpl<T> implements Connections<T> {
	private Map<Integer, ConnectionHandler<T>> _allConnections = new ConcurrentHashMap<Integer, ConnectionHandler<T>>();
	private int ConnectionID=1;
	public void add(ConnectionHandler _handler){
			_allConnections.put(ConnectionID, _handler);
			ConnectionID++;
	}
	
	public int getID(ConnectionHandler _handler){
		for (Map.Entry<Integer, ConnectionHandler<T>> maping: _allConnections.entrySet()){
			if (maping.getValue().equals(_handler))
				return maping.getKey();
		}
		return 0;
	}
	
	
	@Override
	public boolean send(int connectionId, T msg) {
		if (_allConnections.containsKey(connectionId)){
		ConnectionHandler _handler = _allConnections.get(connectionId);
		_handler.send(msg);
		return true;
		}
		return false;
	}
	@Override
	public void broadcast(T msg) {
		for (ConnectionHandler _handler : _allConnections.values()){
			_handler.send(msg);
		}
		
	}

	public void disconnect(int connectionId) {
		_allConnections.remove(connectionId);
		
	}


}
