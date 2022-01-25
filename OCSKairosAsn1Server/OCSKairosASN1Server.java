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
import com.beanit.asn1bean.ber.types.string.BerPrintableString;

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
            int largoReq = ((buf[0] & 0xff) << 8) | (buf[1] & 0xff);

            //Vamos a aceptar mensajes con longitud maxima de 1024 bytes
            if (largoReq < 1024) {
                System.out.println("SERVER: empezando a recibir");                        
                InterfazContenidosKairosSDP peticion = new InterfazContenidosKairosSDP();				
                peticion.decode(socketRequest.getInputStream(),true);
				System.out.println("Peticion.code = " + peticion.code);
                System.out.println("Peticion recibida = " + peticion);
				BDebitKReq cobro = new BDebitKReq();
                ReverseByteArrayOutputStream ostmp = new ReverseByteArrayOutputStream(1024);
                ReverseByteArrayOutputStream osBer = new ReverseByteArrayOutputStream(1024);
                peticion.getMsg().encode(ostmp);
                InputStream istmp = new ByteArrayInputStream(ostmp.getArray());
                cobro.decode(istmp,true);
                istmp.close();
                ostmp.reset(); 
                System.out.println("SERVER: Peticion de COBRO: " + cobro);
                        
                System.out.println("SERVER: preparando respuesta ...");
		System.out.println("SERVER - msisdn leido de la peticion: " + cobro.getSubscriberId());                      
                BDebitKRes rspCobro = new BDebitKRes();				                
				//RejectKRes rspCobro = new RejectKRes();
				//rspCobro.setReleaseCause(new ReleaseCause(new String("-3").getBytes("UTF-8")));				
				//rspCobro.setComment(new BerPrintableString(new String("Error devuelto por mock-OCS").getBytes("UTF-8")));
		rspCobro.setSubscriberId(cobro.getSubscriberId());                        
                rspCobro.encode(ostmp,true);
                rspCobro.code = ostmp.getArray();
                ostmp.close();
                System.out.println("SERVER - Respuesta BDebitKRes: " + rspCobro);
                InterfazContenidosKairosSDP respuesta = new InterfazContenidosKairosSDP();                        
                respuesta.setRequestId(peticion.getRequestId());
                respuesta.setMsgType(new BerInteger(333)); //333 para BDebitKRes; 334 para RejectKRes
                respuesta.setMsg(new BerAny(rspCobro.code));
                System.out.println("SERVER: respuesta -> " + respuesta);
                respuesta.encode(osBer,true);
                respuesta.encode(ostmp,true);  //?
                respuesta.code = ostmp.getArray(); //?
                osBer.close();
                ostmp.close();

				// Creamos el flujo de salida a partir del socket donde escribiremos la trama ASN1 codificada de respuesta
				DataOutputStream os = new DataOutputStream (new BufferedOutputStream(socketRequest.getOutputStream()));    			
                int largoRes = respuesta.code.length;
				os.write(largoRes >> 8);
				os.write(largoRes & 0xff);                        
				os.write(osBer.getArray());
				os.flush();
            } else {
				System.out.println("ERROR: peticióecibida demasiado grande.");
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
