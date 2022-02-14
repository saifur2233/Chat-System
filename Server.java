package muli_user_chat_system;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.StringTokenizer;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

public class Server {

	private JFrame frame;
	private JTextArea contentArea;
	private JTextField txt_message;
	private JTextField txt_max;
	private JTextField txt_port;
	private JButton btn_start;
	private JButton btn_stop;
	private JButton btn_send;
        private JButton btn_clear;
	private JPanel northPanel;
	private JPanel southPanel;
	private JScrollPane rightPanel;
	private JScrollPane leftPanel;
	private JSplitPane centerSplit;

	private JList userList;
	private DefaultListModel listModel;

	private ServerSocket serverSocket;
	private ServerThread serverThread;
	private ArrayList<ClientThread> clients;

	private static ArrayList<String> userNameIps;

	private boolean isStart = false;

	public static void main(String[] args) {
		new Server();
	}

	public void send() {
		if (!isStart) {
			JOptionPane.showMessageDialog(frame, "Server is not running now.", "ERROR", JOptionPane.ERROR_MESSAGE);
			return;
		}
		if (clients.size() == 0) {
			JOptionPane.showMessageDialog(frame, "There is no client here.", "ERROR", JOptionPane.ERROR_MESSAGE);
			return;
		}
		String message = txt_message.getText().trim();
		if (message == null || message.equals("")) {
			JOptionPane.showMessageDialog(frame, "The message should not be empty.", "ERROR",
					JOptionPane.ERROR_MESSAGE);
		}
		// send server msg to all the clients
		sendServerMessage(message);
		contentArea.append("Server: " + txt_message.getText() + "\r\n");
		txt_message.setText(null);
	}

	// constructor
	public Server() {

		frame = new JFrame("Server");

		contentArea = new JTextArea();
		contentArea.setEditable(false);
		contentArea.setForeground(Color.blue);

		txt_message = new JTextField();
		txt_max = new JTextField("30");
		txt_port = new JTextField("6666");

		btn_start = new JButton("Start");
		btn_stop = new JButton("Stop");
		btn_send = new JButton("Send");
                btn_clear = new JButton("Clear");
		btn_stop.setEnabled(false);

		listModel = new DefaultListModel();
		userList = new JList(listModel);

		southPanel = new JPanel(new BorderLayout());
		southPanel.setBorder(new TitledBorder("Write message here:"));
		southPanel.add(txt_message, "Center");
		southPanel.add(btn_send, "East");

		leftPanel = new JScrollPane(userList);
		leftPanel.setBorder(new TitledBorder("Online clients:"));

		rightPanel = new JScrollPane(contentArea);
		rightPanel.setBorder(new TitledBorder("Chatting room:"));

		centerSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightPanel);
		centerSplit.setDividerLocation(130);

		northPanel = new JPanel();
		northPanel.setLayout(new GridLayout(1, 6));
		northPanel.add(new JLabel("Max clients"));
		northPanel.add(txt_max);
		northPanel.add(new JLabel("port"));
		northPanel.add(txt_port);
		northPanel.add(btn_start);
		northPanel.add(btn_stop);
                northPanel.add(btn_clear);
		northPanel.setBorder(new TitledBorder("Setting info:"));

		frame.setLayout(new BorderLayout());
		frame.add(northPanel, "North");
		frame.add(centerSplit, "Center");
		frame.add(southPanel, "South");
		frame.setSize(650, 400);
		int screen_width = Toolkit.getDefaultToolkit().getScreenSize().width;
		int screen_height = Toolkit.getDefaultToolkit().getScreenSize().height;
		frame.setLocation((screen_width - frame.getWidth()) / 2, (screen_height - frame.getHeight()) / 2);
		frame.setVisible(true);

		// when close the window disconnect server
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				if (isStart) {
					closeServer();
				}
				System.exit(0);
			}
		});

		// when press the Enter button on keyboard
		txt_message.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				send();
			}
		});

		// when click "Send" button
		btn_send.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				send();
			}
		});
                
                // when click "Clear" button
		btn_clear.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				contentArea.setText("");
			}
		});

		// when click "Start" button
		btn_start.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				if (isStart) {
					JOptionPane.showMessageDialog(frame, "Server is running, do not restart it!", "ERROR",
							JOptionPane.ERROR_MESSAGE);
					return;
				}
				int max;
				int port;
				try {
					try {
						max = Integer.parseInt(txt_max.getText());
					} catch (Exception e1) {
						throw new Exception("Maximum number of clients should be a positive integer!");
					}
					if (max <= 0) {
						throw new Exception("Maximum number of clients should be a positive integer!");
					}
					try {
						port = Integer.parseInt(txt_port.getText());
					} catch (Exception e1) {
						throw new Exception("Port number should be a positive integer!");
					}
					if (port <= 0) {
						throw new Exception("Port number should be a positive integer!");
					}
					serverStart(max, port);
					contentArea.append("Server is running! Maximum clients: " + max + ", port: " + port + "\r\n");
					JOptionPane.showMessageDialog(frame, "Server is running now!");
					btn_start.setEnabled(false);
					txt_max.setEnabled(false);
					txt_port.setEnabled(false);
					btn_stop.setEnabled(true);
				} catch (Exception exc) {
					JOptionPane.showMessageDialog(frame, exc.getMessage(), "ERROR", JOptionPane.ERROR_MESSAGE);
				}
			}
		});

		// when click the "stop" button
		btn_stop.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				if (!isStart) {
					JOptionPane.showMessageDialog(frame, "Server has not started yet. No need for stop.", "ERROR",
							JOptionPane.ERROR_MESSAGE);
					return;
				}
				try {
					closeServer();
					btn_start.setEnabled(true);
					txt_max.setEnabled(true);
					txt_port.setEnabled(true);
					btn_stop.setEnabled(false);
					contentArea.append("Server stops successfully!\r\n");
					JOptionPane.showMessageDialog(frame, "Server stops successfully!");
				} catch (Exception exc) {
					JOptionPane.showMessageDialog(frame, "Exception occurs when stop the server!", "ERROR",
							JOptionPane.ERROR_MESSAGE);
				}
			}
		});
	}

	public void serverStart(int max, int port) throws BindException {
		try {
			clients = new ArrayList<ClientThread>();
			serverSocket = new ServerSocket(port);
			serverThread = new ServerThread(serverSocket, max);
			serverThread.start();
			isStart = true;
		} catch (BindException e) {
			isStart = false;
			throw new BindException("Port is occupied! Use another one!");
		} catch (Exception e1) {
			e1.printStackTrace();
			isStart = false;
			throw new BindException("Exception occurs when star the server!");
		}
	}

	// close server
	@SuppressWarnings("deprecation")
	public void closeServer() {
		try {
			if (serverThread != null)
				serverThread.stop();// stop the server thread

			for (int i = clients.size() - 1; i >= 0; i--) {
				// send stop command to all the online users
				clients.get(i).getWriter().println("CLOSE");
				clients.get(i).getWriter().flush();
				// release resources
				clients.get(i).stop();// stop this client thread
				clients.get(i).reader.close();
				clients.get(i).writer.close();
				clients.remove(i);
			}
			if (serverSocket != null) {
				serverSocket.close(); // disconnect the server
			}
			listModel.removeAllElements(); // clear the clients list
			isStart = false;
		} catch (IOException e) {
			e.printStackTrace();
			isStart = true;
		}
	}

	// send server msg to all clients
	public void sendServerMessage(String message) {
		for (int i = clients.size() - 1; i >= 0; i--) {
			clients.get(i).getWriter().println("Server: " + message + "\r\n");
			clients.get(i).getWriter().flush();
		}
	}

	class ServerThread extends Thread {
		private ServerSocket serverSocket;
		private int max;

		// constructor
		public ServerThread(ServerSocket serverSocket, int max) {
			this.serverSocket = serverSocket;
			this.max = max;
		}

		public void run() {
			while (true) {
				try {
					Socket socket = serverSocket.accept();
					if (clients.size() == max) {
						BufferedReader r = new BufferedReader(new InputStreamReader(socket.getInputStream()));
						PrintWriter w = new PrintWriter(socket.getOutputStream());
						// receive the basic info of clients
						String inf = r.readLine();
						StringTokenizer st = new StringTokenizer(inf, "@");
						User user = new User(st.nextToken(), st.nextToken());
						// return msg of success
						w.println("MAX@Server: Sorry, " + user.getName() + user.getIp()
								+ ", server is busy now, please try later");
						w.flush();
						// release resources
						r.close();
						w.close();
						socket.close();
						continue;
					}
					ClientThread client = new ClientThread(socket);
					client.start();
					// clients.add(client);
					listModel.addElement(client.getUser().getName());
					contentArea.append(
							client.getUser().getName() + client.getUser().getIp() + " comes to the chatting room!\r\n");
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	class ClientThread extends Thread {
		private Socket socket;
		private BufferedReader reader;
		private PrintWriter writer;
		private User user;

		public BufferedReader getReader() {
			return reader;
		}

		public PrintWriter getWriter() {
			return writer;
		}

		public User getUser() {
			return user;
		}

		// constructor
		public ClientThread(Socket socket) {
			try {
				clients.add(this);
				this.socket = socket;
				reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				writer = new PrintWriter(socket.getOutputStream());
				// receive the basic info from client
				String inf = reader.readLine();
				StringTokenizer st = new StringTokenizer(inf, "@");
				user = new User(st.nextToken(), st.nextToken());
				// return succeed msg
				writer.println(user.getName() + user.getIp() + " is connected to the server now!");
				writer.flush();
				// send msg about connection result
				if (clients.size() > 0) {
					String temp = "";
					ArrayList<String> userNameIps = new ArrayList<>();
					for (int i = clients.size() - 1; i >= 0; i--) {
						temp = "USERLIST@" + clients.size() + "@" + clients.get(i).getUser().getName() + "@"
								+ clients.get(i).getUser().getIp();
						userNameIps.add(temp);
					}
					for (int i = 0; i < userNameIps.size(); i++) {
						writer.println(userNameIps.get(i).toString());
						writer.flush();
					}
				}
				// tell all the clients that this client is coming
				for (int i = clients.size() - 1; i >= 0; i--) {
					clients.get(i).getWriter().println("ADD@" + user.getName() + "@" + user.getIp());
					clients.get(i).getWriter().flush();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		@SuppressWarnings("deprecation")
		public void run() {
			// keep waiting for the connection
			String message = null;
			while (true) {
				try {
					message = reader.readLine();
					if (message.equals("CLOSE")) { // close command
						System.out.println(this.getUser().getIp());
						contentArea.append(
								this.getUser().getName() + this.getUser().getIp() + " leaves the chatting room!\r\n");
						// disconnect, release resource
						reader.close();
						writer.close();
						socket.close();
						// tell all the clients that this client is getting off
						for (int i = clients.size() - 1; i >= 0; i--) {
							clients.get(i).getWriter().println("DELETE@" + user.getName());
							clients.get(i).getWriter().flush();
						}
						listModel.removeElement(user.getName()); // update the client list
						// delete the thread for this client
						for (int i = clients.size() - 1; i >= 0; i--) {
							if (clients.get(i).getUser() == user) {
								ClientThread temp = clients.get(i);
								clients.remove(i); // delete the thread for the client
								temp.stop(); // stop the thread
								return;
							}
						}
					} else {
						dispatcherMessage(message); // dispatch msg
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		// dispatch msg
		public void dispatcherMessage(String message) {
			StringTokenizer stringTokenizer = new StringTokenizer(message, "@");
			int count = 1;
			String source = null;
			String content = null;
			while (stringTokenizer.hasMoreTokens()) {
				String data = stringTokenizer.nextToken();
				if (count == 1) {
					source = data;
				}
				if (count == 3) {
					content = data;
				}
				count++;
			}
			message = source + ": " + content;
			contentArea.append(message + "\r\n");
			for (int i = clients.size() - 1; i >= 0; i--) {
				clients.get(i).getWriter().println(message + "\r\n");
				clients.get(i).getWriter().flush();
			}
		}
	}
}
