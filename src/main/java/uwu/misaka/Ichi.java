package uwu.misaka;

import arc.func.Prov;
import arc.util.Time;
import arc.util.async.AsyncExecutor;
import mindustry.net.Host;
import mindustry.net.NetworkIO;

import java.awt.*;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;

public class Ichi {
    static final AsyncExecutor executor = new AsyncExecutor(Math.max(Runtime.getRuntime().availableProcessors(), 6));
    static final Prov<DatagramPacket> packetSupplier = () -> new DatagramPacket(new byte[512], 512);
    
    public static String hook = "meme";
    public static String name = "meme";
    public static String avatar = "meme";

    public static void main(String[] a) throws InterruptedException {
        ArrayList<pingableHost> hosts = new ArrayList<>();
        hosts.add(new pingableHost("obvilionnetwork.ru",7000));
        hosts.add(new pingableHost("185.13.47.140",1001));
        hosts.add(new pingableHost("shizashizashiza.ml",27736));

        while(true){
            Thread.sleep(10000);
            hosts.forEach(h->h.ping());
        }
    }
    public static class pingableHost{
        public String ip;
        public int port;
        public String succesMessage;
        public String dropMessage;
        public boolean booted;

        public pingableHost(String ip,int p){
            this.ip=ip;
            this.port = p;
            this.succesMessage = ip+":"+p+" проснулся, можно отдыхать";
            this.dropMessage = ip+":"+p+" спит,ора будить";
            booted=true;
        }

        public void ping(){
            pingHost(this);
        }
    }
    public static void pingHost(pingableHost h){
        executor.submit(() -> {
            try{
                DatagramSocket socket = new DatagramSocket();
                long time = Time.millis();
                socket.send(new DatagramPacket(new byte[]{-2, 1}, 2, InetAddress.getByName(h.ip), h.port));
                socket.setSoTimeout(2000);

                DatagramPacket packet = packetSupplier.get();
                socket.receive(packet);

                ByteBuffer buffer = ByteBuffer.wrap(packet.getData());
                Host host = NetworkIO.readServerData((int)Time.timeSinceMillis(time), packet.getAddress().getHostAddress(), buffer);
                System.out.println(h.ip+":"+h.port +"("+InetAddress.getByName(h.ip)+") "+host.name+" "+host.description+" "+ host.mapname+" "+host.ping);
                if(h.booted==true){
                    return;
                }else{
                    handleUp(h);
                    h.booted=true;
                }
            }catch(Exception ignored){
                if(h.booted==false){
                    return;
                }else{
                    handleDrop(h);
                    h.booted=false;
                }
                try {
                    System.out.println(h.ip + ":" + h.port + "(" + InetAddress.getByName(h.ip) + ") clowned");
                }catch(Exception e){
                    System.out.println(h.ip + ":" + h.port + "(clowned ip) clowned");
                }
            }
        });
    }

    public static void handleDrop(pingableHost h){
        DiscordWebhook wh = new DiscordWebhook(hook);
        wh.setUsername(name);
        wh.setAvatarUrl(avatar);
        wh.addEmbed(new DiscordWebhook.EmbedObject()
                .setTitle(h.dropMessage)
                .setColor(new Color(110, 237, 139)));
        try {
            wh.execute();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        } finally {
            wh=null;
        }
    }
    public static void handleUp(pingableHost h){
        DiscordWebhook wh = new DiscordWebhook(hook);
        wh.setUsername(name);
        wh.setAvatarUrl(avatar);
        wh.addEmbed(new DiscordWebhook.EmbedObject()
                .setTitle(h.succesMessage)
                .setColor(new Color(110, 237, 139)));
        try {
            wh.execute();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        } finally {
            wh=null;
        }
    }
}
