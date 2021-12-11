package application;
	
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Iterator;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Font;
import javafx.stage.Stage;


public class Main extends Application {
	
	public static ExecutorService threadPooL;
	public static Vector<Client> clients = new Vector<Client>();
	
	ServerSocket serverSocket;
	
	public void startServer(String IP, int port) {
		try {
			serverSocket = new ServerSocket();
			serverSocket.bind(new InetSocketAddress(IP,port));
		}catch(Exception e) {
			e.printStackTrace();
			if(!serverSocket.isClosed()) {
				stopServer();
			}
			return;
		}
		Runnable thead = new Runnable() {

			@Override
			public void run() {
				while(true) {
					try {
						Socket socket = serverSocket.accept();
						clients.add(new Client(socket));
						System.out.println("[Ŭ���̾�Ʈ ����]"
								+socket.getRemoteSocketAddress()
								+": "+Thread.currentThread().getName());
					}catch(Exception e) {
						if(!serverSocket.isClosed()) {
							stopServer();
						}
						break;
					}
				}
				
			}
		};
		threadPooL = Executors.newCachedThreadPool();
		threadPooL.submit(thead);
	}
	
	public void stopServer() {
		try {
			Iterator<Client> iterator = clients.iterator();
			while(iterator.hasNext()) {
				Client client = iterator.next();
				client.socket.close();
				iterator.remove();
			}
			if(serverSocket != null && !serverSocket.isClosed()) {
				serverSocket.close();
			}
			if(threadPooL != null && !threadPooL.isShutdown()) {
				threadPooL.shutdown();
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * javafx GUI
	 */
	@Override
	public void start(Stage primaryStage) throws Exception {
		BorderPane root = new BorderPane(); // ���̾ƿ�
		root.setPadding(new Insets(5));
		
		TextArea textArea = new TextArea();
		textArea.setEditable(false);
		textArea.setFont(new Font("D2",15));
		root.setCenter(textArea);
		
		Button toggleButton = new Button("�����ϱ�");
		toggleButton.setMaxWidth(Double.MAX_VALUE);
		BorderPane.setMargin(toggleButton, new Insets(1,0,0,0));
		root.setBottom(toggleButton);
		
		String IP = "127.0.0.1";
		int port = 9876;
		
		toggleButton.setOnAction(event ->{
			if(toggleButton.getText().equals("�����ϱ�")) {
				startServer(IP, port);
				Platform.runLater(()->{
					String message = String.format("[���� ����]\r\n",IP,port);
					textArea.appendText(message);
					toggleButton.setText("�����ϱ�");
				});
			}else {
				stopServer();
				Platform.runLater(()->{
					String message = String.format("[���� ����]\r\n",IP,port);
					textArea.appendText(message);
					toggleButton.setText("�����ϱ�");
				});
			}
		});
		Scene scene = new Scene(root,400,400);
		primaryStage.setTitle("[ä�ü���]");
		primaryStage.setOnCloseRequest(event -> stopServer());
		primaryStage.setScene(scene);
		primaryStage.show();
	}
	public static void main(String[] args) {
		launch(args);
	}
}