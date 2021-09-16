package estudiante;
 
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import org.jgroups.Address;
 
import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.ReceiverAdapter;
import org.jgroups.View;
import org.jgroups.demos.Draw;
import org.jgroups.util.Util;
 
public class SimpleChat {
	private JChannel channel;
        private JChannel channel2;
	private List<String> state = new LinkedList<String>();
        private List<String> state2 = new LinkedList<String>();
	private String userName = System.getProperty("user.name", "guchao");
        private String nombre;
        Address destination = null;
        Draw pizarra=null;
 
	public void start(DefaultListModel<Address> l1, JList<Address> lista, JTextArea texto) throws Exception {
        nombre = JOptionPane.showInputDialog(null,"Ingrese su nombre");
	channel = new JChannel();
        channel.name(nombre);
		channel.setReceiver(new ReceiverAdapter() {
			// Método de devolución de llamada para recibir mensajes
                        @Override
			public void receive(Message msg) {
				System.out.println(msg.getSrc() + ": " + msg.getObject());
                                if(!msg.getSrc().toString().equals(nombre))
                                    texto.append(msg.getSrc() + ": " + msg.getObject()+"\n");
				synchronized(state) {
					state.add((String)msg.getObject());
				}
			}
			 // Método de devolución de llamada, este método puede obtener nueva información de vista
                        @Override
			public void viewAccepted(View view) {
                                lista.clearSelection();
				System.out.println("view accepted: " + view);
                                List<Address> list= view.getMembers();
                                l1.clear(); 
                                for(Address nombre:list){
                                    l1.addElement(nombre);
                                }
			}
 
			public byte[] getState() {
				synchronized(state) {
					try {
						return Util.objectToByteBuffer(state);
					}
					catch(Exception e) {
						e.printStackTrace();
						return null;
					}
				}
			}
 
			@SuppressWarnings("unchecked")
			public void setState(byte[] new_state) {
				try {
					List<String> list=(List<String>)Util.objectFromByteBuffer(new_state);
					synchronized(state) {
						state.clear();
						state.addAll(list);
					}
					System.out.println("received state (" + list.size() + " messages in chat history):");
					for(String str: list) {
						System.out.println(str);
					}
				}
				catch(Exception e) {
					e.printStackTrace();
				}
			}
		});
		channel.connect("ChatCluster");
		channel.getState(null, 10000);
 
		//
		sendMessage();
 
		//
		channel.close();
	}
        
        public void startSlate() throws Exception{
            channel2=new JChannel();
            channel2.name(channel.getName());
            channel2.setReceiver(new ReceiverAdapter() {
			// Método de devolución de llamada para recibir mensajes
                        @Override
			public void receive(Message msg) {
				synchronized(state2) {
					state2.add((String)msg.getObject());
				}
			}
			 // Método de devolución de llamada, este método puede obtener nueva información de vista
                        @Override
			public void viewAccepted(View view) {
				System.out.println("view accepted: " + view);
                                List<Address> list= view.getMembers();
			}
 
			public byte[] getState() {
				synchronized(state2) {
					try {
						return Util.objectToByteBuffer(state2);
					}
					catch(Exception e) {
						e.printStackTrace();
						return null;
					}
				}
			}
 
			@SuppressWarnings("unchecked")
			public void setState(byte[] new_state) {
				try {
					List<String> list=(List<String>)Util.objectFromByteBuffer(new_state);
					synchronized(state2) {
						state2.clear();
						state2.addAll(list);
					}
					System.out.println("received state (" + list.size() + " messages in chat history):");
					for(String str: list) {
						System.out.println(str);
					}
				}
				catch(Exception e) {
					e.printStackTrace();
				}
			}
		});
            channel2.connect("SlateCluster");
            channel2.getState(null, 10000);
        }
 
	private void sendMessage() throws Exception {
		boolean succeed = false;
		BufferedReader br = null;
		try {
			br = new BufferedReader(new InputStreamReader(System.in));
			while(true) {
                        String line = br.readLine();
                            if(line != null && line.equals("exit")) {
                                    break;
                            }
                        }
			succeed = true;
		} finally {
			if(br != null) {
				try {
					br.close();
				} catch (Exception e) {
					if(succeed) {
						throw e;
					}
				}
			}
		}
	}
 
	public static void main(String args[]) throws Exception {
            SimpleChat go=new SimpleChat();
            
            JFrame v=new JFrame("Ventana");
            v.setLocation(200, 200);
            v.setSize(590, 400);
            v.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            JPanel panel= new JPanel();
            JPanel panel2= new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
            
            JTextArea texto = new JTextArea(17,28);
            texto.setEditable(false);
            JTextArea texto1 = new JTextArea(3,28);
            JScrollPane scroll1=new JScrollPane(texto);
            JScrollPane scroll2=new JScrollPane(texto1);
            JButton boton = new JButton("Enviar");
            JButton boton2=new JButton("Pizarra");
            
            DefaultListModel<Address> l1 = new DefaultListModel<>();
            JList<Address> list = new JList<>(l1);
            list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            list.setBorder(javax.swing.BorderFactory.createTitledBorder("Conectados"));
            list.setEnabled(false);
            JScrollPane scroll3=new JScrollPane(list);
            
            panel.add(scroll1);
            panel.add(scroll2);
            panel2.add(boton);
            panel2.add(boton2);
            panel.add(panel2);
            
            v.add(panel, BorderLayout.WEST);
            v.add(scroll3, BorderLayout.EAST);
            v.setResizable(false);
            v.setVisible(true);
            
            boton.addActionListener(new ActionListener()
            {
              @Override
              public void actionPerformed (ActionEvent e)
              {
                    String line = texto1.getText();
                    
                    Message msg = new Message(go.destination, line);
                    try {
                        go.channel.send(msg);
                        texto1.setText("");
                        texto.append(line+"\n");
                    } catch (Exception ex) {
                        Logger.getLogger(SimpleChat.class.getName()).log(Level.SEVERE, null, ex);
                    }     
              }
            });
            
            boton2.addActionListener(new ActionListener()
            {
              @Override
              public void actionPerformed (ActionEvent e)
              {
                  try {
                      go.startSlate();
                      go.pizarra=new Draw(go.channel2);
                      go.pizarra.go();
                  } catch (Exception ex) {
                      Logger.getLogger(SimpleChat.class.getName()).log(Level.SEVERE, null, ex);
                  }
              }
            });
            
            go.start(l1,list,texto);
	}
}