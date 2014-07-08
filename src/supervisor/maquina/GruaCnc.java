/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package supervisor.maquina;

import Bases.cnc;
import java.text.DecimalFormat;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.util.Duration;


/**
 *
 * @author Francisco
 */
public class GruaCnc {
    //Eventos
    //Coneccion al dispositivo
    OnConnectionListener conListener;
    public interface OnConnectionListener{
        public void OnConnectionSucces();
        public void OnConnectionExistsErr();
        public void OnConnectionMaxLimitErr();
        public void OnConnectionSocketErr();
        public void OnConnectionTcpErr();
    }
    
    public void setOnConnectionListener(OnConnectionListener cmdLst){
        conListener=cmdLst;
    }
    //Movimiento
    OnMovementListener movListener;
    public interface OnMovementListener{
        public void OnCommandFailed(String fuente);//fallo el enviar el comando de movimiento
        public void OnMovementSucces();//movimiento libre en XY finalizado
        public void OnGoToWagonSucces();//movimiento de aproximación a un vagon finalizado
        public void OnMixingSucces();//ciclo de mezclado completo
        public void OnMixingStepSucces();//desplazamiento de mezclado en el vagon finalizado
        public void OnMixingAproachSucces();//Aproximacíon e introducción del mezclador finalizada
        public void OnMixerRetractSucces();//extracción del mezclador finalizada
        public void OnGoToSamplingPointSucces();//llegada a un punto de muestreo en coordenadas relativas al vagon
        public void OnPositionUpdate(Double x,Double y,Double z, Double r);//se actualizo la informacion sobre la posición del equipo
        public void OnDeviveIsBussy();//el equipo esta ocupado 
    }
    
    public void setOnOnMovementListener(OnMovementListener mvLst){
        movListener=mvLst;
    }
    
    
    
    //Clase de control del equipo
    //de momento la dll se inicializara en la clase principal luego se verá si trasladar eso a esta clase
    
    cnc control=new cnc();
    // constante para codigos de error
    /*Codigos de error para la funcion de coneccion
    0-->no error
    1-->existe la conección
    2-->maximo numero de conecciones alcanzado
    3-->errror de socket 
    4-->fallo en la coneccion TCP
    */
    public final int COM_NO_ERR=0;
    public final int COM_ERR_CON_EXISTS=1;
    public final int COM_ERR_MAX_CON=2;
    public final int COM_ERR_SOCKET=3;
    public final int COM_ERR_TCP=4;
    
    /*Codigos de error para los demas parametros
    -1-->fallo al enviar
    0-->correcto
    1-->codigo de funcion incorrecto o no soportado
    2-->direccion invalida o no doportada
    3-->datos invalidos o no soportados
    4-->fallo en la ejecución del movimiento
    5-->movimiento en ejecución(la operacion puede esperar)
    6-->el equipo esta ocupado no puede ejecutar la orden de momento
    8-->error de comprobacion de archivo
    10-->ruta a puerta de acceso invalida
    11-->el dispoditivo objetivo no responde
    224-->error de transmicion o marco de datos de modbus invalido
    255-->equipo tarda mucho en responder(dispositivo no responde o no esta conectado a la red)
    225-->Movimiento no definido
    226-->fallo en la apertura del archivo
    227-->Error de direccion de base
    */  
    public final int NO_ERR=0;
    public final int MODBUS_SND_FAIL=-1;
    public final int MODBUS_INVALID_FUNC=1;
    public final int MODBUS_INVALID_ADDR=2;
    public final int MODBUS_INVALID_DATA=3;       
    public final int MODBUS_PERFORM_FAIL=4;
    public final int MODBUS_DEVICE_ACK=5;
    public final int MODBUS_DEVICE_BUSY=6;
    public final int MODBUS_MEM_PARITY_ERR=8;
    public final int MODBUS_INVALID_GATEWAY_PATH=10;
    public final int MODBUS_DEVICE_NO_RESPOND=11;
    public final int MODBUS_FRAME_ERR=224;
    public final int MODBUS_TIMEOUT_ERR=255;
    public final int MODBUS_PFM_UNDEFINED=225;
    public final int MODBUS_OPEN_FILE_FAIL=226;
    public final int MODBUS_BASEADDR_ERR=227;
    //variables de comunicación
    private String ip;
    private Short port;
    Task<Integer> task;
    Thread th;
    //Variables de estado del equipo
    public BooleanProperty ocupado=new SimpleBooleanProperty(false);
    ////////////////////////////////////////////////////////    
    public SimpleBooleanProperty conected=new SimpleBooleanProperty(false);
    //Temporizador de actualizacion de datos de posición y estado
    Timeline actualizador = new Timeline(new KeyFrame(Duration.millis(500), new EventHandler<ActionEvent>() {
    @Override
    public void handle(ActionEvent event) {
    //Temporizador para control de posición
    //implementar funcion de actualizacion de datos    
        actualizar();
        
        if(!ocupado.get()){
            actualizador.stop();
        }
    }
    }));

    //Posición de la punta del agitador 
    public Double posX; //metros
    public Double posY; //metros
    public Double posZ; //metros
    public Double posR; //grados de inclinación del agitador
    //Posición de la punta del agitador como propiedades para muestra en interfaz de usuario
    public StringProperty show_posX=new SimpleStringProperty("Desconocido");
    public StringProperty show_posY=new SimpleStringProperty("Desconocido");
    public StringProperty show_posZ=new SimpleStringProperty("Desconocido");
    public StringProperty show_posR=new SimpleStringProperty("Desconocido");
    //Velocidades de los diferentes ejes
    public Double velX; //m/s
    public Double velY; //m/s
    public Double velZ; //m/s
    public Double velR; //grados/s
    //Posición de la punta del agitador como propiedades para muestra en interfaz de usuario
    public StringProperty show_velX=new SimpleStringProperty("Desconocido");
    public StringProperty show_velY=new SimpleStringProperty("Desconocido");
    public StringProperty show_velZ=new SimpleStringProperty("Desconocido");
    public StringProperty show_velR=new SimpleStringProperty("Desconocido");
    //Posición de los diferentes elementos del equipo en metros o grados según aplique
    public Double posPortico;
    public Double posCarro;
    public Double posElevador;
    public Double posRotador;
    //Posición de los diferentes ejes del equipo como propiedades para muestra de datos
    public StringProperty show_posPortico=new SimpleStringProperty("Desconocido");
    public StringProperty show_posCarro=new SimpleStringProperty("Desconocido");
    public StringProperty show_posElevador=new SimpleStringProperty("Desconocido");
    public StringProperty show_posRotador=new SimpleStringProperty("Desconocido");
    //Posición del equipo en pulsos 
    public Integer pX;
    public Integer pY;
    public Integer pZ;
    public Integer pR;
    //Constantes del equipo del equipo,son solo para pruebas debe verificarse con la física final del equipo
    public final Double x0; //distancia entre el punto de origen y el eje de inclinacion del cabezal en eje x
    public final Double y0; //distancia entre el punto de origen y el eje de inclinacion del cabezal en eje y
    public final Double z0; //distancia entre el punto de origen y el eje de inclinacion del cabezal en eje z
    public final Double la; //longitud del agitador
    public final Double Rx; //Relacion revoluciones motor vs desplazamiento en metros metros en x
    public final Double Ry; //Relacion revoluciones motor vs desplazamiento en metros metros en y
    public final Double Rz; //Relacion revoluciones motor vs desplazamiento en metros del elevador 
    public final Double Rr; //Relacion revoluciones motor vs Grados actuador
    public final Double Rp; //Relacion Pulsos/Revolución
    public final Double Rv; //Relacion vmaquina/
    
    /////////////////////////////////////////////////////////////////////////////////
    //sistemas de configuración
    DecimalFormat df=new DecimalFormat("0.00");
    
    //Constructor, de momento solo se asegura de inicializar las constantes propias del equipo relacionadas con su dimesionamiento
    public GruaCnc(Double x_0,
            
            Double y_0,
            Double z_0,
            Double l_a,
            Double r_x,
            Double r_y,
            Double r_z,
            Double r_r,
            Double r_p,
            Double r_v){
        
        control.init();
        this.x0=x_0;
        this.y0=y_0;
        this.z0=z_0;
        this.la=l_a;
        this.Rx=r_x;
        this.Ry=r_y;
        this.Rz=r_z;
        this.Rr=r_r;
        this.Rp=r_p;
        this.Rv=r_v;
        
        actualizador.setCycleCount(Timeline.INDEFINITE);
    }   
    //Metodos

    public void conectar_asinc(String dir,int puerto){

        ip=dir;
        port=(short)puerto;        
        task=new Task<Integer>() {
            @Override
            protected Integer call() throws Exception {
                return control.modbus_connect(ip, port);
            }
            
        };
        task.onSucceededProperty().addListener(new ChangeListener(){
        @Override
            public void changed(ObservableValue ov, Object t, Object t1) {
                System.out.println("Fin del intento de conección :");
            try {
                switch(task.get()){
                    case (int)COM_NO_ERR:{
                        conListener.OnConnectionSucces();
                        break;
                    }
                    case COM_ERR_CON_EXISTS:{
                        conListener.OnConnectionExistsErr();
                        break;
                    }
                    case COM_ERR_MAX_CON:{
                        conListener.OnConnectionMaxLimitErr();
                        break;
                    }
                    case COM_ERR_SOCKET:{
                        conListener.OnConnectionSocketErr();
                        break;
                    }
                    case COM_ERR_TCP:{
                        conListener.OnConnectionTcpErr();
                        break;
                    }
                    
                }
            } catch (InterruptedException ex) {
                Logger.getLogger(GruaCnc.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ExecutionException ex) {
                Logger.getLogger(GruaCnc.class.getName()).log(Level.SEVERE, null, ex);
            }
            }
        });
        
        th=new Thread(task);
        th.setDaemon(true);
        th.start();
    }
    
    /**
     * Funcion de movimiento general de la maquina
     * @param x posicion absoluta del eje x del equipo
     * @param y posición absoluta del eje y del equipo
     * @param z posición absoluta del eje z del equipo
     * @param r posición absoluta del eje r del equipo
     */
    public void mover(Double x,Double y, Double z,Double r){
        if(!ocupado.get()){
            int pulsos1;
            int pulsos2;
            int pulsos3;
            int pulsos4;
            int pulsos5;
            
            pulsos1=(int)(x*Rx*Rp);
            pulsos2=pulsos1;
            pulsos3=(int)(y*Ry*Rp);
            pulsos4=(int)(z*Rz*Rp);
            pulsos5=(int)(r*Rr*Rp);
            
            //int res=control.fifo(pulsos1, pulsos2, pulsos3, pulsos4, pulsos5, (int)(166.6667*1000));
            //int res=control.fifo((short)3, pulsos3, (short)4, pulsos4, (short)5, pulsos5, (int)(166.6667*1000));
            int res=control.line((short)3, pulsos3, (short)4, pulsos4, (short)5, pulsos5);
            
            if(res==0){
                System.out.println("Comando enviado con exito");
                actualizador.play();
                ocupado.set(true);
            }
            else{
                System.out.println("Fallo : "+res );
            }
            
        }
        else{
            movListener.OnDeviveIsBussy();
        }
        
    }
    public void moverXY(){//solo desplazamiento del carro

    }
    public void irVagon(){
 
    }
    public void mezclarVagon(){

    }
    public void etapaMezclado(){

    }
    public void aproximacionMezclado(){

    }
    public void retirarMezclador(){

    }
    public void irPuntoMuestreo(){
        
    }
    
    //////////////////////////
    //Supervisor de movimiento
     private void actualizar(){
        int res =control.is_working();
        if(res!=2){
            if(res==1){
                ocupado.set(true);
            }
            else{
                ocupado.set(false);
            }
        }
        if(control.get_axis_pos()==0){
            int pulsos1=control.get_pos1();
            int pulsos2=control.get_pos2();
            int pulsos3=control.get_pos3();
            int pulsos4=control.get_pos4();
            int pulsos5=control.get_pos5();
            
            
            //de momento no se usaran los valores de velocidad


            if(Math.abs(pulsos1-pulsos2)>100){
                //control.stop();
                 System.out.println();
                System.out.println("Desfase entre motores eje X :"+Math.abs(pulsos1-pulsos2));
            }
            
            pX=pulsos1;
            pY=pulsos3;
            pZ=pulsos4;
            pR=pulsos5;
            
            posPortico=pX.doubleValue()/(Rx*Rp);
            posCarro=pY.doubleValue()/(Ry*Rp);
            posElevador=pZ.doubleValue()/(Rz*Rp);
            posRotador=pR.doubleValue()/(Rr*Rp);
           
            movListener.OnPositionUpdate(posPortico, posCarro, posElevador, posRotador);
            
            show_posPortico.set(df.format(posPortico));
            show_posCarro.set(df.format(posCarro));
            show_posElevador.set(df.format(posElevador));
            show_posRotador.set(df.format(posRotador));
            
            //actualizar estado del equipo, si libre u ocupado
            
            int reswk;
            reswk=control.is_working();
            if(reswk!=2){
                if(reswk==1){
                    ocupado.set(true);
                }
                else{
                    ocupado.set(false);
                    movListener.OnMovementSucces();
                    //filtrar los casos de error.....
                }
            }
            
            
        }
        else{
             System.out.println("Fallo al actualizar");
        }
     }
        
    
}
