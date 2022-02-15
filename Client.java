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
import java.net.InetAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileSystemView;

public class Client {

	private JFrame frame;
	private JTextArea textArea;
        private JTextPane textPane;
	private JTextField textField;
	private JTextField txt_port;
	private JTextField txt_hostIp;
	private JTextField txt_name;
	private JButton btn_start;
	private JButton btn_stop;
	private JButton btn_send;
        private JButton btn_clear;
        private JButton btn_file;
	private JPanel northPanel;
	private JPanel southPanel;
	private JScrollPane rightScroll;
	private JScrollPane leftScroll;
	private JSplitPane centerSplit;
        private JRadioButton oneToNRadioBtn;
	private JRadioButton broadcastBtn;
        String id, clientIds = "";

	private JList userList;
	private DefaultListModel listModel;

	private boolean isConnected = false;

	private Socket socket;
	private PrintWriter writer;
	private BufferedReader reader;
	private MessageThread messageThread; // thread for receiving msg
	private Map<String, User> onLineUsers = new HashMap<String, User>(); // all the online users

	public static void main(String[] args) {
		new Client();
	}

	// execute send
	public void send() {
		if (!isConnected) {
			JOptionPane.showMessageDialog(frame, "Not connected yet, can't send message!", "ERROR",
					JOptionPane.ERROR_MESSAGE);
			return;
		}
		String message = textField.getText().trim();
		if (message == null || message.equals("")) {
			JOptionPane.showMessageDialog(frame, "The message should not be empty.", "ERROR",
					JOptionPane.ERROR_MESSAGE);
			return;
		}
		sendMessage(frame.getTitle() + "@" + "ALL" + "@" + message);
		// textArea.append(frame.getTitle() + ": " + textField.getText() + "\r\n");
		textField.setText(null);
	}

	// constructor
	public Client() {

		textArea = new JTextArea();
		textArea.setEditable(false);
		textArea.setForeground(Color.blue);
                
                oneToNRadioBtn = new JRadioButton("1-to-N");
                broadcastBtn = new JRadioButton("Multicast");

		textField = new JTextField();
		txt_port = new JTextField("6666");
		txt_hostIp = new JTextField("192.168.0.109");
		txt_name = new JTextField("Saifur");

		btn_start = new JButton("Start");
		btn_stop = new JButton("Stop");
		btn_send = new JButton("Send");
                btn_clear = new JButton("Clear");
                btn_file = new JButton("File");

		listModel = new DefaultListModel();
		userList = new JList(listModel);

		northPanel = new JPanel();
		northPanel.setLayout(new GridLayout(1, 7));
		northPanel.add(new JLabel("Port"));
		northPanel.add(txt_port);
		northPanel.add(new JLabel("Server IP"));
		northPanel.add(txt_hostIp);
		northPanel.add(new JLabel("Name"));
		northPanel.add(txt_name);
		northPanel.add(btn_start);
		northPanel.add(btn_stop);
                northPanel.add(btn_clear);
                northPanel.add(btn_file);
		northPanel.setBorder(new TitledBorder("Connection info:"));

		rightScroll = new JScrollPane(textArea);
		rightScroll.setBorder(new TitledBorder("Chatting room:"));
		leftScroll = new JScrollPane(userList);
		leftScroll.setBorder(new TitledBorder("Online clients:"));
                

		southPanel = new JPanel(new BorderLayout());
		southPanel.add(textField, "Center");
		southPanel.add(btn_send, "East");
                //southPanel.add(btn_file);
                
		southPanel.setBorder(new TitledBorder("Write message here:"));

		centerSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftScroll, rightScroll);
		centerSplit.setDividerLocation(130);

		frame = new JFrame("Client");

		frame.setLayout(new BorderLayout());
		frame.add(northPanel, "North");
		frame.add(centerSplit, "Center");
		frame.add(southPanel, "South");
		frame.setSize(700, 400);
		int screen_width = Toolkit.getDefaultToolkit().getScreenSize().width;
		int screen_height = Toolkit.getDefaultToolkit().getScreenSize().height;
		frame.setLocation((screen_width - frame.getWidth()) / 2, (screen_height - frame.getHeight()) / 2);
		frame.setVisible(true);

		// when press the Enter button on keyboard
		textField.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				// TODO Auto-generated method stub
				send();
			}
		});

		// when click "Send" button
		btn_send.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
                                String textAreaMessage = textField.getText().trim();
                                if (textAreaMessage != null && !textAreaMessage.isEmpty()) {
                                String messageToBeSentToServer = "";
				
                                
                                List<String> clientList = userList.getSelectedValuesList();
                                System.out.println("selected user"+ clientList);
                                if (clientList.size() == 0){
                                    send(); // send to all
                                }// if no user is selected then set the flag for further use
                                else if(clientList.size() > 0) {
                                    
                                    //textArea.append(frame.getTitle() + ": " + textAreaMessage + "\r\n");
                                 send(); // send to all
                                }else{
                                    send(); // send to all
                                }  
				
                                }
                                else{
                                    JOptionPane.showMessageDialog(frame, "Please write a message!", "ERROR",
                                JOptionPane.ERROR_MESSAGE);
                                    // textfield has no msg
                                }
				
			}
		});
                
                // when click "Clear" button
		btn_clear.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				textArea.setText("");
			}
		});
                
                // when click "File" button
		btn_file.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
		             JFileChooser j = new JFileChooser(FileSystemView.getFileSystemView());
                                // Open the save dialog
                                j.showSaveDialog(null);
                                
			}
		});

		// when click "Start" button
		btn_start.addActionListener((ActionEvent e) -> {
                    // TODO Auto-generated method stub
                    int port;
                    if (isConnected) {
                        JOptionPane.showMessageDialog(frame, "Already connected, do not reconnect it!", "ERROR",
                                JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    try {
                        try {
                            port = Integer.parseInt(txt_port.getText().trim());
                        } catch (NumberFormatException e2) {
                            throw new Exception("Port number should be a positive integer!");
                        }
                        if (port <= 0) {
                            throw new Exception("Port number should be a positive integer!");
                        }
                        String hostIp = txt_hostIp.getText().trim();
                        String name = txt_name.getText().trim();
                        if (name.equals("") || hostIp.equals("")) {
                            throw new Exception("Name, HostIP should not be empty!");
                        }
                        boolean flag = connectServer(port, hostIp, name);
                        if (flag == false) {
                            throw new Exception("Connection failed!");
                        }
                        frame.setTitle(name);
                        JOptionPane.showMessageDialog(frame, "Connection succeed!");
                    } catch (Exception exc) {
                        JOptionPane.showMessageDialog(frame, exc.getMessage(), "ERROR", JOptionPane.ERROR_MESSAGE);
                    }
                });

		// when click the "stop" button
		btn_stop.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				System.out.println("stop pressed");
				if (!isConnected) {
					JOptionPane.showMessageDialog(frame, "Already stoped. No need for stop.", "ERROR",
							JOptionPane.ERROR_MESSAGE);
					return;
				}
				try {
					boolean flag = closeConnection();
					if (flag == false) {
						throw new Exception("Exception occurs when stop!");
					}
					JOptionPane.showMessageDialog(frame, "Stopped!");
				} catch (Exception exc) {
					JOptionPane.showMessageDialog(frame, exc.getMessage(), "ERROR", JOptionPane.ERROR_MESSAGE);
				}
			}
		});

		// when close the window disconnect server
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				if (isConnected) {
					closeConnection();
				}
				System.exit(0);
			}
		});
	}

	public boolean connectServer(int port, String hostIp, String name) {
		// connect to server
		try {
			InetAddress address = InetAddress.getByName(hostIp);
			boolean reachable = address.isReachable(10000);
			System.out.println("Is host reachable? " + reachable);

			socket = new Socket(InetAddress.getByName(hostIp), port);
			writer = new PrintWriter(socket.getOutputStream());
			reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			// send basic info of the client
			sendMessage(name + "@" + socket.getLocalAddress().toString());
			// start message thread
			messageThread = new MessageThread(reader, textArea);
			messageThread.start();
			isConnected = true;
			return true;
		} catch (Exception e) {
			textArea.append("Connection fialed! port: " + port + " hostIp: " + hostIp + "\r\n");
                        
			isConnected = false;
			return false;
		}
	}

	public void sendMessage(String message) {
		writer.println(message);
		writer.flush();
	}

	// client disconnect from server
	@SuppressWarnings("deprecation")
	public synchronized boolean closeConnection() {
		try {
			sendMessage("CLOSE");
			listModel.removeAllElements();
			messageThread.stop();
			// release resources
			if (reader != null) {
				reader.close();
			}
			if (writer != null) {
				writer.close();
			}
			if (socket != null) {
				socket.close();
			}
			isConnected = false;
			return true;
		} catch (IOException e1) {
			e1.printStackTrace();
			isConnected = true;
			return false;
		}
	}

	class MessageThread extends Thread {
		private BufferedReader reader;
		private JTextArea textArea;

		// constructor
		public MessageThread(BufferedReader reader, JTextArea textArea) {
			this.reader = reader;
			this.textArea = textArea;
		}

		// client's connection is closed passively
		public synchronized void closeCon() throws Exception {
			// clear list
			listModel.removeAllElements();
			// release resources
			if (reader != null) {
				reader.close();
			}
			if (writer != null) {
				writer.close();
			}
			if (socket != null) {
				socket.close();
			}
			isConnected = false;
		}

		public void run() {
			String message = "";
			while (true) {
				try {
					message = reader.readLine();
					System.out.println(message);
					StringTokenizer stringTokenizer = new StringTokenizer(message, "@");
					String command = "";
					if (stringTokenizer.hasMoreTokens()) {
						command = stringTokenizer.nextToken();
					}

					String[] tokens = message.split("@");
					if (command.equals("CLOSE")) {
						textArea.append("Server is closed.\r\n");
						closeCon();
						return; // thread stops
					} else if (command.equals("ADD")) { // when new client gets online, update the list
						String username = "";
						String userIp = "";
						if ((username = tokens[1]) != null && (userIp = tokens[2]) != null) {
							User user = new User(username, userIp);
							onLineUsers.put(username, user);
							listModel.addElement(username);
							// System.out.println(listModel.getElementAt(0).toString());
						}
					} else if (command.equals("DELETE")) { // when client gets offline, update the list
						String username = tokens[1];
						User user = (User) onLineUsers.get(username);
						onLineUsers.remove(user);
						listModel.removeElement(username);
					} else if (command.equals("USERLIST")) { // load the online client
						String username = null;
						String userIp = null;
						int count = 1;
						String un = null;
						String ui = null;
						while (stringTokenizer.hasMoreTokens()) {
							String data = stringTokenizer.nextToken();
							if (count == 2) {
								un = data;
							}
							if (count == 3) {
								ui = data;
							}
							count++;
						}
						User user = new User(un, ui);
						onLineUsers.put(un, user);
						listModel.addElement(un);
					} else if (command.equals("MAX")) { // when client no. increased to the maximum
						textArea.append("Chatting room is full, please come later!" + "\r\n");
						closeCon();
						JOptionPane.showMessageDialog(frame, "Chatting room is full!", "ERROR",
								JOptionPane.ERROR_MESSAGE);
						return; // close the thread
					} else {
						textArea.append(message + "\r\n");
					}
				} catch (IOException e) {
					e.printStackTrace();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
}
