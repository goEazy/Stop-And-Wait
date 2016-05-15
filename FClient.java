import java.io.*;
import java.net.*;
import java.util.*;
import java.nio.ByteBuffer;


class FClient
{
    private static final int BUFF_SIZE = 512;
    private static final int PACKET_SIZE = 525;
    public static void main(String args[]) throws IOException
    {
        String beg = "";
        String end = "";
        String last = "";
        int ack_no = 0;
        int seq_no = 0;
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        DatagramSocket socket = null;
        try
        {
            DatagramPacket sp = null;
            DatagramPacket rp = null;
            socket = new DatagramSocket();
            InetAddress ip = InetAddress.getByName(args[0]);
            int port = Integer.parseInt(args[1]);
            System.out.println("connected to server");
            
            String fileName = args[2];     
            int fnl = (int)fileName.length();
            byte[] fn = new byte[fnl];
            fn = fileName.getBytes();
            sp = new DatagramPacket(fn, fn.length, ip, port);
            socket.send(sp);
        
            File file = new File("copy_"+fileName);
            FileOutputStream fos = new FileOutputStream(file);
            
            while(!end.equalsIgnoreCase("END"))
            {
                byte[] buff = new byte[PACKET_SIZE];
                rp = new DatagramPacket(buff, PACKET_SIZE);
                socket.receive(rp);
                byte[] st = new byte[6];
                byte[] en = new byte[7];
                for(int i = 0; i < 6; i++)
                {
                    st[i] = buff[i];
                }
                beg = new String(st);
                seq_no = Integer.parseInt(beg.substring(4,5));
                System.out.println("ACK " + ack_no + " SEQ " + seq_no);
                if(seq_no == ack_no)
                {
                    for(int i = 0; i < BUFF_SIZE; i++)
                    {
                        //if(buff[i + 6] != '\0')
                        //{
                            fos.write(buff[i + 6]);
                        //}
                    }
                    ack_no = (ack_no + 1)%2;
                }
                for(int i = 0; i < 7; i++)
                {
                    en[i] = buff[518 + i];
                }
                last = new String(en);
                end = last.substring(1,4);
                String reply = "ACK " + ack_no + " \n\r";
                System.out.println(reply);
                byte[] sd = reply.getBytes();
                sp = new DatagramPacket(sd, sd.length, ip, port);
                socket.send(sp);
            }
            fos.close();
        }
        catch(Exception e)
        {
            e.printStackTrace();
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