import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.io.*;
import com.beanit.asn1bean.ber.ReverseByteArrayOutputStream;
import com.beanit.asn1bean.ber.types.BerAny;
import com.beanit.asn1bean.ber.types.BerInteger;

import kairos.ocs.*;

import java.io.IOException;

class OCSKairosClientServiceThread implements Runnable{
    
        private final Socket socketRequest;
        private final AtomicInteger count;

    public OCSKairosClientServiceThread(final Socket req, final AtomicInteger count){
        this.socketRequest = req;
        this.count = count;
    }
  
    public void run(){
        try {
            byte[] buf = new byte[2];
            if (socketRequest.getInputStream().read(buf) < 2) {
                    //ERROR no podemos leer longitud completa
                    return;
            }
            int largo = ((buf[0] & 0xff) << 8) | (buf[1] & 0xff);

            //Vamos a aceptar mensajes con longitud maxima de 1024 bytes
            if (largo < 1024) {
                        System.out.println("SERVER: empezando a recibir");
                        BDebitKReq cobro = new BDebitKReq();
                        InterfazContenidosKairosSDP peticion = new InterfazContenidosKairosSDP();
                        peticion.decode(socketRequest.getInputStream(),true);
                        System.out.println(peticion);
                        ReverseByteArrayOutputStream ostmp = new ReverseByteArrayOutputStream(1024);
                        ReverseByteArrayOutputStream osBer = new ReverseByteArrayOutputStream(5000);
                        peticion.getMsg().encode(ostmp);
                        InputStream istmp = new ByteArrayInputStream(ostmp.getArray());
                        cobro.decode(istmp,true);
                        istmp.close();
                        ostmp.reset();
                        System.out.println("SERVER: Req: " + cobro);
                        
                        System.out.println("SERVER: preparando respuesta ...");
                        BDebitKRes rspCobro = new BDebitKRes();
                        String numero = cobro.getSubscriberId().toString();
                        //System.out.println("SERVER - msisdn leido: " + numero);
                        rspCobro.setSubscriberId(new McMsisdn(numero.getBytes("UTF-8")));                        
                        //CodigoError3 error3 = new CodigoError3();
                        //error3.value = new byte[] { (byte)0x30, (byte)0x31 };                         
                        //RspConsultaTotalSaldo.Element2 elemento2 = new RspConsultaTotalSaldo.Element2();
                        //elemento2.setCodRetorno(error3);
                        //rspConsulta.setElement2(elemento2);
                        //rspConsulta.encode(osBer);
                        rspCobro.encode(ostmp,true);
                        rspCobro.code = ostmp.getArray();
                        ostmp.reset();
                        //osBer.reset();
                        System.out.println("SERVER - rsp: " + rspCobro);
                        //respuesta.setMsg(new BerAny(rspCobro.code));
                        //System.out.println("SERVER: rsp -> " + respuesta);                        
                        //respuesta.encode(osBer,true);
                        InterfazContenidosKairosSDP respuesta = new InterfazContenidosKairosSDP();
                        //respuesta.setRequestId(new BerInteger(1));
                        respuesta.setRequestId(peticion.getRequestId());
                        respuesta.setMsgType(new BerInteger(333));
                        respuesta.setMsg(new BerAny(rspCobro.code));
                        System.out.println("SERVER: respuesta -> " + respuesta);
                        respuesta.encode(osBer,true);
                        respuesta.encode(ostmp,true);
                        respuesta.code = ostmp.getArray();
                        osBer.close();
                        ostmp.close();

                        largo = respuesta.code.length;
                        socketRequest.getOutputStream().write(largo >> 8);
                        socketRequest.getOutputStream().write(largo & 0xff);
                        //socketRequest.getOutputStream().write(buf);
                        socketRequest.getOutputStream().write(respuesta.code);
                        //socketRequest.getOutputStream().write(osBer.getArray());
                        socketRequest.getOutputStream().flush();
            }
        }catch(SocketTimeoutException ex) {
            //Aqui se llega cuando se tardan mas de 2 segundos en un bloque
            System.out.println("Se tardan en mandar datos, adios");
            try {
                    socketRequest.getOutputStream().write("ERROR".getBytes());
                    socketRequest.getOutputStream().flush();
            } catch (IOException e2) {
                   e2.printStackTrace();
            }
        }catch(IOException ex) {            
        	ex.printStackTrace();
        }finally {
            try {
                    socketRequest.close();
            } catch (IOException ex) {
                    ex.printStackTrace();
            }
            count.getAndDecrement();
        }
    }
    
} //OCSKairosClientServiceThread

public class OCSKairosASN1Server {

        //protected static int serverPort   = 9999;
        //protected static ServerSocket serverSocket = null;
        protected boolean isStopped    = false;
        protected Thread runningThread= null;
        //Vamos a usar un thread pool con 50 hilos
        //Asi que solamente se van a procesar hasta 50 conexiones de manera simultanea
        //static ExecutorService threadPool = Executors.newFixedThreadPool(50);
        //El limite de conexiones que vamos a recibir
        //Arbitrariamente definimos 10 por hilo
        //static int max = 500;
        //Contaremos los hilos en funcionamiento
        //static AtomicInteger count = new AtomicInteger(0);

        public static void main(String[] args) {
                try{
                        int puerto=9999;
                        if (args.length != 1){
                                System.out.println("Error. Un parametro (solo) es necesario con el puerto de escucha.");
                        }else{
                                puerto = Integer.parseInt(args[0]);
                                final ServerSocket servidor = new ServerSocket(puerto);
                                ExecutorService threadPool = Executors.newFixedThreadPool(5);
                                AtomicInteger count = new AtomicInteger(0);
                                int max = 50;
                                System.out.println("Arrancando servidor.");
                                //OCSKairosASN1Server servidor = new OCSKairosASN1Server(puerto);
                                //new Thread(servidor).start();
                                //try {
                                //      Thread.sleep(20 * 1000);
                                //} catch (InterruptedException e) {
                                //      e.printStackTrace();
                                //}
                                //System.out.println("Stopping Server");
                                //servidor.stop();
                                while (true) {
                                //Primero tenemos este ciclo para esperar a que
                                //baje el numero de conexiones abiertas
                                while (count.get() > max) {
                                        System.out.println(String.format("Servidor saturado, esperando desconexiones para bajar de %d", count.get()));
                                        //Si hay saturacion, esperamos medio segundo
                                        try {
                                                Thread.sleep(500);
                                        } catch (InterruptedException ex) {
                                                //manejar
                                        }
                                }
                                //Aqui llegamos solamente cuando no hay saturacion
                                Socket socket = servidor.accept();
                                System.out.println(String.format("Nueva conexion (van %d)", count.incrementAndGet()));
                                //Mandamos la conexion a correr en el thread pool
                                threadPool.execute(new OCSKairosClientServiceThread(socket, count));
                                }                                
                        }//else
                }catch (Exception e){
                        System.out.println(e);
                }
        }

}
