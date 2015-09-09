/*
 * Project 5
 * @author Josh Radochonski, jradocho, 808
 * */

import java.io.*;
import java.net.*;
import java.util.*;

public class SafeWalkServer implements Runnable {
    int portNumber;
    ArrayList<Person> clientList = new ArrayList<Person>();
    String[] commandList = { ":LIST_PENDING_REQUESTS", ":RESET", ":SHUTDOWN" };
    String[] fromList = { "CL50", "EE", "LWSN", "PMU", "PUSH" };
    String[] toList = { "CL50", "EE", "LWSN", "PMU", "PUSH", "*" };
    ServerSocket serverSocket;
    
    public SafeWalkServer(int portNumber) throws IOException
    {
        this.portNumber = portNumber;
        serverSocket = new ServerSocket(portNumber);
    }
    
    public SafeWalkServer() throws IOException
    {
        Random randNum = new Random();
        int port = randNum.nextInt(65535-1025) + 1025;
        if (port > 1025 && port < 65535)
        {
            this.portNumber = port;
        }
        serverSocket = new ServerSocket(portNumber);
    }
    
    public static void main(String[] args) throws IOException, ClassNotFoundException 
    {
        if (args.length == 0)
        {
            Random randNum = new Random();
            int port = randNum.nextInt(65535-1025) + 1025;
            if (port > 1025 && port < 65535)
            {
                SafeWalkServer s = new SafeWalkServer(port);
            }
        }
        else
        {
            if (Integer.parseInt(args[0]) > 1025 && Integer.parseInt(args[0]) < 65535)
            {
                SafeWalkServer s = new SafeWalkServer(Integer.parseInt(args[0]));
            }
            else
            {
                Random randNum = new Random();
                int port = randNum.nextInt(65535-1025) + 1025;
                if (port > 1025 && port < 65535)
                {
                    SafeWalkServer s = new SafeWalkServer(port);
                }
            }
        }
    }
    /*
     * Start a loop to accept incoming connections.
     */
    public void run() 
    {
        while (true) 
        {
            try
            {
                Socket socket = serverSocket.accept();
                
                PrintWriter pw = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String s = br.readLine();
                
                boolean isValid = false;
                if (s.charAt(0) == ':') 
                {
                    int i;
                    for (i = 0; i < commandList.length; i++) 
                    {
                        if (s.equals(commandList[i])) 
                        {
                            isValid = true;
                            break;
                        }
                    }
                    if (i == 0) 
                    {
                        String list = listPendingRequests();
                        pw.println(list);
                    } 
                    else if (i == 1) 
                    {
                        
                        resetServer();
                        pw.println("RESPONSE: success");
                        
                    } 
                    else if (i == 2)
                    {
                        shutDownServer();
                        pw.println("RESPONSE: success");
                        break;
                    }
                    else
                    {

                        String str = "ERROR: invalid request";
                        pw.println(str);
                    }
                } 
                else 
                {
                    
                    boolean valid = addPerson(s, socket);
                    if (!valid)
                    {
                        pw.println("ERROR: invalid request");
                        socket.close();
                    }
                    checkCompatibility();
                }
            }
            catch (IOException e) {}
            catch (ClassNotFoundException c) {}
            
        }
    }
    
    private void checkCompatibility() throws IOException
    {
        if (clientList.size() >= 2)
        {
            Person addedPerson = clientList.get(clientList.size() - 1);
            for (int i = 0; i < clientList.size() - 1; i ++)
            {
                Person tempPerson = clientList.get(i);
                PrintWriter tempPW = new PrintWriter(tempPerson.getSocket().getOutputStream(), true);
                PrintWriter addPW = new PrintWriter(addedPerson.getSocket().getOutputStream(), true);
                if (tempPerson.getFrom().equals(addedPerson.getFrom()))
                {
                    if (tempPerson.getTo().equals(addedPerson.getTo()) && !tempPerson.getTo().equals("*"))
                    {
                        //"RESPONSE: Dinushi,LWSN,PUSH,0"
                        tempPW.println("RESPONSE: " + addedPerson.getName() + "," + addedPerson.getFrom() + "," 
                                           + addedPerson.getTo() + "," + addedPerson.getPriority());
                        addPW.println("RESPONSE: " + tempPerson.getName() + "," + tempPerson.getFrom() + "," 
                                           + tempPerson.getTo() + "," + tempPerson.getPriority());
                        clientList.remove(clientList.size() - 1);
                        clientList.remove(i);
                        break;
                    }
                    else if ((tempPerson.getTo().equals("*") && !addedPerson.getTo().equals("*")) || (!tempPerson.getTo().equals("*") && addedPerson.getTo().equals("*")))
                    {
                        //success
                        tempPW.println("RESPONSE: " + addedPerson.getName() + "," + addedPerson.getFrom() + "," 
                                           + addedPerson.getTo() + "," + addedPerson.getPriority());
                        addPW.println("RESPONSE: " + tempPerson.getName() + "," + tempPerson.getFrom() + "," 
                                           + tempPerson.getTo() + "," + tempPerson.getPriority());
                        clientList.remove(clientList.size() - 1);
                        clientList.remove(i);
                        break;
                    }
                }
            }
        }
    }
    
    private String listPendingRequests() 
    {
        String list = "";
        list += String.format("[");
        for (int i = 0; i < clientList.size(); i++) 
        {
            Person temp = clientList.get(i);
            list += String.format("[%s, %s, %s, %d]", temp.name, temp.from, temp.to, temp.priority);
            if (i + 1 < clientList.size()) 
            {
                list += String.format(", ");
            }
        }
        list += String.format("]");
        return list;
    }
    
    private void resetServer() throws IOException, ClassNotFoundException 
    {
        for (int i = 0; i < clientList.size(); i ++)
        {
            
            Socket tempSocket = clientList.get(i).getSocket();
            PrintWriter pw = new PrintWriter(tempSocket.getOutputStream(), true);
            pw.println("ERROR: connection reset");
            
            pw.close();
            tempSocket.close();
        }
        clientList.clear();
    }
    
    private void shutDownServer()  throws IOException, ClassNotFoundException 
    {
        resetServer();
    }
    
    private boolean addPerson(String s, Socket t) 
    {
        int countCommas = 0;
        String name = "";
        String from = "";
        String to = "";
        String str = s;
        for (int i = 0; i < str.length(); i ++)
        {
            if (str.charAt(i) == ',')
            {
                countCommas ++;
            }
        }
        if (countCommas != 3)
        {
            return false;
        }
        for (int n = 0; n < str.length(); n ++)
        {
            if (str.charAt(n) != ',')
            {
                name += str.charAt(n);
            }
            else
            {
                str = str.substring(n + 1, str.length() - 1);
                break;
            }
        }
        
        for (int n = 0; n < str.length(); n ++)
        {
            if (str.charAt(n) != ',')
            {
                from += str.charAt(n);
            }
            else
            {
                str = str.substring(n + 1, str.length() - 1);
                break;
            }
        }
        
        for (int n = 0; n < str.length(); n ++)
        {
            if (str.charAt(n) != ',')
            {
                to += str.charAt(n);
            }
            else
            {
                str = str.substring(n + 1, str.length() - 1);
                break;
            }
        }
        
        boolean fromvalid = false;
        boolean tovalid = false;
        for (int i = 0; i < fromList.length; i ++)
        {
            if (fromList[i].equals(from))
            {
                fromvalid = true;
                break;
            }
        }
        for (int i = 0; i < toList.length; i ++)
        {
            if (toList[i].equals(to))
            {
                tovalid = true;
                break;
            }
        }
        if (tovalid && fromvalid && !to.equals(from))
        {
            Person p = new Person(name, from, to, t);
            clientList.add(p);
            return true;
        }
        else
        {
            return false;
        }
    }
    
    /*
     * Return the port number on which the server is listening.
     */
    public int getLocalPort() {
        return portNumber;
    }
}

class Person {
    Socket t;
    String name = "";
    String from = "";
    String to = "";
    int priority = 0;
    
    Person(String name, String from, String to, Socket t) 
    {
        this.name = name;
        this.from = from;
        this.to = to;
        this.t = t;
    }
    
    String getName() 
    {
        return name;
    }
    
    Socket getSocket() 
    {
        return t;
    }
    
    String getFrom()
    {
        return from;
    }
    
    String getTo()
    {
        return to;
    }
    
    int getPriority()
    {
        return priority;
    }
}
