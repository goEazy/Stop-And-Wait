import java.net.*;  
import java.io.*;
import java.nio.ByteBuffer;
import java.util.*;
import java.nio.Buffer;
public class FServer
{
    
    private static final int BUFF_SIZE = 512;
    private static final int PACKET_SIZE = 525;
    private static final double LOSS_RATE = 0.1;
    private static final int AVERAGE_DELAY = 100;
    public static void main(String[] args) throws IOException 
    {
        int seq_no = 0;
        int ack_no = 0;
        int numSequences;
        DatagramSocket socket = null;
        Random random = new Random();
        try
        {
            int port = Integer.parseInt(args[0]);
            socket = new DatagramSocket(port);
            socket.setSoTimeout(1000);
            
            System.out.println("Connecting to Client...");
            while(true)
            {
                try
            {
                    DatagramPacket rp = null;
                    DatagramPacket sp = null;

                    //getting filename
                    rp = new DatagramPacket(new byte[100], 100);
                    socket.receive(rp);
                    String fileName = printData(rp).trim();
                    System.out.println(fileName);

                    //getting Client's IP and Port
                    int clientPort = rp.getPort();
                    InetAddress clientHost = rp.getAddress();

                    //extracting file and converting into bytes array
                    File file = new File(fileName);
                    FileInputStream fis = new FileInputStream(file);
                    int size = (int)file.length();
                    numSequences = (int)size/BUFF_SIZE + 1;
                    byte[] sendBytes = new byte[size];
                    fis.read(sendBytes);

                    int count = 0;
                    
                    while(count < (numSequences - 1))
                    {
                        if (random.nextDouble() < LOSS_RATE)
                        {
                            System.out.println("    Reply not sent.");
                            continue; 
                        }
                        byte[] buff = new byte[PACKET_SIZE];
                        String ss = "RDT " + seq_no + " ";
                        String ee = " \n\r";
                        byte[] zz = ss.getBytes();
                        byte[] yy = ee.getBytes();

                        int k = 0;
                        for(int i = 0; i < 6; i++)
                        {
                            buff[k++] = zz[i];
                        }
                        for(int i = 0; i < BUFF_SIZE; i++)
                        {
                             buff[k++] = sendBytes[count*BUFF_SIZE + i];
                        }
                        for(int i = 0; i < 3; i++)
                        {
                            buff[k++] = yy[i];
                        }
                        for(int i = 0; i < 4; i++)
                        {
                            buff[k++] = '\0';
                        }
                        sp = new DatagramPacket(buff, PACKET_SIZE, clientHost, clientPort);
                        socket.send(sp);

                        byte[] rv = new byte[100];
                        rp = new DatagramPacket(rv, rv.length);
                        System.out.println("Waiting for Reply from Receiver...");
                        boolean continueSending = true;
                        while(continueSending)
                        {
                            try
                            {
                                socket.receive(rp);
                                continueSending = false;
                                String replyMessage = printData(rp);
                                count++;
                                ack_no = Integer.parseInt(replyMessage.substring(4,5));
                                System.out.println("SEQ " + seq_no + " ACK " + ack_no + "..");
                                seq_no = ack_no;
                            }
                            catch(SocketTimeoutException e)
                            {
                                System.out.println("    Resending Data");
                                sp = new DatagramPacket(buff, PACKET_SIZE, clientHost, clientPort);
                                socket.send(sp);
                            }

                        }

}
                    if(size > count*BUFF_SIZE)
                    {
                        try
                        {
                            boolean continueSending = true;
                            byte[] buff = new byte[PACKET_SIZE];
                            String ss = "RDT " + seq_no + " ";
                            String ee = " END \n\r";
                            byte[] zz = ss.getBytes();
                            byte[] yy = ee.getBytes();

                            int k = 0;
                            for(int i = 0; i < 6; i++)
                            {
                                buff[k++] = zz[i];
                            }
                            for(int i = 0; i < (size - count*BUFF_SIZE); i++)
                            {
                                buff[k++] = sendBytes[count*BUFF_SIZE + i];
                            }
                            for(; k < 518;)
                            {
                                buff[k++] = '\0';
                            }
                            
                            for(int i = 0; i < 7; i++)
                            {
                                buff[k++] = yy[i];
                            }
                            String yuy = new String(buff);
                            System.out.println(yuy);
                            sp = new DatagramPacket(buff, PACKET_SIZE, clientHost, clientPort);
                            socket.send(sp);
                            byte[] rv = new byte[100];
                            rp = new DatagramPacket(rv, rv.length);
                            while(continueSending)
                            {

                                try
                                {
                                    socket.receive(rp);
                                    continueSending = false;
                                    String replyMessage = printData(rp);
                                    count++;
                                    ack_no = Integer.parseInt(replyMessage.substring(4,5));
                                    System.out.println("SEQ " + seq_no + " ACK " + ack_no);
                                    seq_no = ack_no;
                                }
                                catch(SocketTimeoutException e)
                                {
                                    sp = new DatagramPacket(buff, PACKET_SIZE, clientHost, clientPort);
                                    socket.send(sp);
                                }

                            }
                        }
                        catch(Exception e)
                        {
                            e.printStackTrace();
                        }
                        
                    }
                    break;
                }
                catch(SocketTimeoutException e){}
                
            }
            
        }
        catch(Exception e)
        {
            System.out.println("Connection Error");
        }
        finally
        {
            socket.close();
        }
    }
    
    private static String printData(DatagramPacket request) throws IOException
    {
        BufferedReader br = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(request.getData())));
        String line = br.readLine();
        return line;
    }
}  