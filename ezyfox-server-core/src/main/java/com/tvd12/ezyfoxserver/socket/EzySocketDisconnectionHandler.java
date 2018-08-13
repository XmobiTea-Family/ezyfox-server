package com.tvd12.ezyfoxserver.socket;

import static com.tvd12.ezyfox.util.EzyProcessor.processWithLogException;

import com.tvd12.ezyfox.constant.EzyConstant;
import com.tvd12.ezyfoxserver.entity.EzySession;

import lombok.Setter;

public class EzySocketDisconnectionHandler extends EzySocketAbstractEventHandler {

    @Setter
	protected EzySocketDisconnectionQueue disconnectionQueue;
    @Setter
    protected EzySocketDataHandlerGroupRemover dataHandlerGroupRemover;
	
	@Override
    public void handleEvent() {
	    processDisconnectionQueue();
	}
	
	@Override
	public void destroy() {
	    processWithLogException(disconnectionQueue::clear);
	}
	
	private void processDisconnectionQueue() {
		try {
			EzySocketDisconnection disconnection = disconnectionQueue.take();
			processDisconnection(disconnection);
		} 
		catch (InterruptedException e) {
			getLogger().warn("disconnection-handler thread interrupted: " + Thread.currentThread());
		}
		catch(Throwable throwable) {
			getLogger().warn("problems in disconnection-handler main loop, thread: " + Thread.currentThread(), throwable);
		}
	}
	
	private void processDisconnection(EzySocketDisconnection disconnection) {
	    try {
	        processDisconnection0(disconnection);
	    }
	    finally {
            disconnection.release();
        }
	}
	
	private void processDisconnection0(EzySocketDisconnection disconnection) {
	    EzySession session = disconnection.getSession();
	    EzyConstant disconnectReason = disconnection.getDisconnectReason();
	    EzySocketDataHandlerGroup handlerGroup = removeDataHandlerGroup(session);
	    if(handlerGroup != null) {
	        handlerGroup.fireChannelInactive(disconnectReason);
	    }
	    else { 
	        getLogger().warn("has no handler group with session: " + session + ", ignore disconnection: " + disconnection);
	    }
	}
	
	protected EzySocketDataHandlerGroup removeDataHandlerGroup(EzySession session) {
	    return dataHandlerGroupRemover.removeHandlerGroup(session);
	}
	
}
