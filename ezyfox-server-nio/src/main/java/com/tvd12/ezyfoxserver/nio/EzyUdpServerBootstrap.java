package com.tvd12.ezyfoxserver.nio;

import static com.tvd12.ezyfox.util.EzyProcessor.processWithLogException;

import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;

import com.tvd12.ezyfox.builder.EzyBuilder;
import com.tvd12.ezyfox.util.EzyDestroyable;
import com.tvd12.ezyfox.util.EzyStartable;
import com.tvd12.ezyfoxserver.context.EzyServerContext;
import com.tvd12.ezyfoxserver.nio.constant.EzyNioThreadPoolSizes;
import com.tvd12.ezyfoxserver.nio.socket.EzyNioAcceptableConnectionsHandler;
import com.tvd12.ezyfoxserver.nio.socket.EzyNioSocketAcceptor;
import com.tvd12.ezyfoxserver.nio.socket.EzyNioSocketReader;
import com.tvd12.ezyfoxserver.nio.upd.EzyNioUdpReadingLoopHandler;
import com.tvd12.ezyfoxserver.nio.wrapper.EzyHandlerGroupManager;
import com.tvd12.ezyfoxserver.setting.EzySettings;
import com.tvd12.ezyfoxserver.setting.EzyUdpSetting;
import com.tvd12.ezyfoxserver.socket.EzySocketEventLoopHandler;
import com.tvd12.ezyfoxserver.socket.EzySocketEventLoopOneHandler;

public class EzyUdpServerBootstrap implements EzyStartable, EzyDestroyable {

	private Selector readSelector;
	private DatagramSocket datagramSocket;
	private DatagramChannel datagramChannel;
	private EzySocketEventLoopHandler readingLoopHandler;
	
	protected EzyServerContext serverContext;
	protected EzyHandlerGroupManager handlerGroupManager;
	
	public EzyUdpServerBootstrap(Builder builder) {
		this.serverContext = builder.serverContext;
		this.handlerGroupManager = builder.handlerGroupManager;
	}
	
	@Override
	public void start() throws Exception {
		openSelectors();
		newAndConfigServerDatagramChannel();
		getBindAndConfigServerSocket();
		startSocketHandlers();
	}
	
	@Override
	public void destroy() {
		processWithLogException(() -> readingLoopHandler.destroy());
		processWithLogException(() -> datagramSocket.close());
		processWithLogException(() -> datagramChannel.close());
	}
	
	private void openSelectors() throws Exception {
		this.readSelector = openSelector();
	}
	
	private void newAndConfigServerDatagramChannel() throws Exception {
		this.datagramChannel = DatagramChannel.open();
	    this.datagramChannel.configureBlocking(false);
	    
	}
	
	private void getBindAndConfigServerSocket() throws Exception {
		this.datagramSocket = datagramChannel.socket();
		this.datagramSocket.setReuseAddress(true);
		this.datagramSocket.bind(new InetSocketAddress(getUdpAddress(), getUdpPort()));
		this.datagramChannel.register(readSelector, SelectionKey.OP_READ);
	}
	
	private void startSocketHandlers() throws Exception {
		EzyNioSocketAcceptor socketAcceptor = new EzyNioSocketAcceptor();
		readingLoopHandler = newReadingLoopHandler(socketAcceptor);
		readingLoopHandler.start();
	}
	
	private EzySocketEventLoopHandler newReadingLoopHandler(
			EzyNioAcceptableConnectionsHandler acceptableConnectionsHandler) {
		EzySocketEventLoopOneHandler loopHandler = new EzyNioUdpReadingLoopHandler();
		loopHandler.setThreadPoolSize(getSocketReaderPoolSize());
		EzyNioSocketReader eventHandler = new EzyNioSocketReader();
		eventHandler.setOwnSelector(readSelector);
		eventHandler.setHandlerGroupManager(handlerGroupManager);
		eventHandler.setAcceptableConnectionsHandler(acceptableConnectionsHandler);
		loopHandler.setEventHandler(eventHandler);
		return loopHandler;
	}
	
	private Selector openSelector() throws Exception {
		return Selector.open();
	}
	
	private int getSocketReaderPoolSize() {
		return EzyNioThreadPoolSizes.SOCKET_READER;
	}
	
	private int getUdpPort() {
		return getUdpSetting().getPort();
	}
	
	private String getUdpAddress() {
		return getUdpSetting().getAddress();
	}
	
	private EzyUdpSetting getUdpSetting() {
		return getServerSettings().getUdp();
	}
	
	private EzySettings getServerSettings() {
		return serverContext.getServer().getSettings();
	}
	
	public static Builder builder() {
		return new Builder();
	}
	
	public static class Builder implements EzyBuilder<EzyUdpServerBootstrap> {

		protected EzyServerContext serverContext;
		protected EzyHandlerGroupManager handlerGroupManager;
		
		public Builder serverContext(EzyServerContext context) {
			this.serverContext = context;
			return this;
		}
		
		public Builder handlerGroupManager(EzyHandlerGroupManager manager) {
			this.handlerGroupManager = manager;
			return this;
		}
		
		@Override
		public EzyUdpServerBootstrap build() {
			return new EzyUdpServerBootstrap(this);
		}
	}
}